## Context

Greenfield repository: OpenSpec scaffolding only, no Android application code. See `proposal.md` for motivation and the three delta specs for observable behavior.

Constraints that shape the approach:

- Product is Android-only; the home-screen widget is the UI.
- v1 measures the default network path and refreshes only on tap.
- Lists are bundled now; a later change will add a network-backed source.
- No backend in this change.

## Goals / Non-Goals

**Goals:**

- One Android module with a widget, a technical launcher activity, a check pipeline, and a bundled list file.
- Keep list loading and refresh triggers behind small interfaces so remote lists, network-change, and periodic refresh can attach later without rewriting verdicts or widget fields.
- Keep a single check under a tight time budget by stopping a group once majority is known.

**Non-Goals:**

- iOS, settings, per-probe diagnostics, remote list fetch, WorkManager periodic jobs, and `ConnectivityManager` callbacks (except reading current validated-network state).
- Detecting provider block-page bodies or distinguishing VPN from “everything open”.
- Forcing a cellular `Network` while Wi-Fi is active.

## Decisions

### 1. Kotlin App Widget with RemoteViews, not Glance

**Choice:** Classic `AppWidgetProvider` + `RemoteViews`, Kotlin, minSdk 26, a single app module.

**Why:** Three colored cells, a status line, and one tap target are a stable RemoteViews layout. Glance adds another runtime for little gain on a non-Compose widget. minSdk 26 is enough for `NET_CAPABILITY_VALIDATED`.

**Alternatives:** Jetpack Glance (nicer composition, more launcher variance); Compose only for the widget (not how home-screen widgets work).

The launcher activity can still be a single Compose or XML screen; that choice does not affect the widget contract.

### 2. Layered pipeline, triggers sit outside

```
  [TapTrigger]  (NetworkChange / Periodic later)
          |
          v
     RefreshPipeline
          |
          +-- no validated network --> WidgetState.NoNetwork
          |
          v
     ListSource --> ProbeEngine --> majority (early stop)
          |
          v
     WidgetState.Ready --> AppWidgetManager.updateAppWidget
```

`RefreshPipeline` is the only entry used by the widget. Tap is a `PendingIntent` that starts a short-lived service or coroutine owner, which calls the pipeline. Future triggers call the same entry.

Validated network is `ConnectivityManager` active network + `NET_CAPABILITY_VALIDATED`. If that is missing, skip probes.

### 3. HTTPS GET, any status is success, 5s per address

**Choice:** HTTPS GET on the default client, 5 second connect/read timeout, consume the status line, then close. 2xx/3xx/4xx/5xx all count as success. No body parsing. Redirects need not be followed: a 3xx is already an HTTP response.

**Why:** Matches “HTTP response = success” without treating a site-level 403 as “closed”. Early close keeps the 15-probe worst case smaller.

**Alternatives:** TLS-only (weaker “opens in a browser” signal); 2xx-only (lies on normal redirects); HEAD (some hosts mishandle HEAD).

Stop a group when successes or failures reach `floor(n/2)+1`. Remaining calls in that group can be cancelled.

### 4. Versioned JSON behind ListSource

Bundled asset, same schema a remote file can reuse later:

```
{
  "version": 1,
  "groups": {
    "whitelist": [ "...", "...", "...", "...", "..." ],
    "ordinary":  [ "...", "...", "...", "...", "..." ],
    "blocked":   [ "...", "...", "...", "...", "..." ]
  }
}
```

`ListSource` returns the three groups. v1 implementation reads the asset only. Widget and `ProbeEngine` never open the file themselves.

Starter addresses (roles, not a permanent registry):

| Group     | Role                                      | Starter HTTPS origins                                      |
|-----------|-------------------------------------------|------------------------------------------------------------|
| whitelist | Lives in whitelist-only mode              | gosuslugi.ru, yandex.ru, vk.com, mail.ru, sberbank.ru      |
| ordinary  | Lives under everyday blocking             | wikipedia.org, github.com, habr.com, cloudflare.com, apple.com |
| blocked   | Typically closed under everyday blocking  | instagram.com, facebook.com, x.com, discord.com, linkedin.com |

Use `https://` URLs. Adjust hostnames (www vs apex) during implementation if a canary fails TLS/SNI on the canonical name.

### 5. Widget chrome

- Default size about 4×1; three equal cells with Russian labels **Белый**, **Обычный**, **Запрет**.
- Available = green, not available = red, in-progress / unused cells during no-network = gray.
- Status line: **Проверяем…** while running; **Нет сети** plus time when there is no validated network; otherwise last-check time (`HH:mm`).
- Whole widget is one tap target bound to refresh, not to the launcher activity.

Persist last `WidgetState` so a process death after a check still shows the last verdicts until the next tap.

### 6. Tests stay around the rules, not the launcher

Unit-test majority/early-stop and “HTTP status vs timeout” with a fake transport. Unit-test `ListSource` size/oddness on the bundled file. Do not leave “explore the codebase” tasks; this repo has no app code yet.

## Risks / Trade-offs

- **[Canny sites rot]** → Bundled lists will drift; `ListSource` + versioned JSON is the hook for a later remote file. Until then, treat the starter table as replaceable data.
- **[Block page counts as open]** → Any HTTP response is success, including a provider stub. Accepted for v1; body heuristics are out of scope.
- **[VPN looks like “everything open”]** → Correct for “what opens for me right now”; do not add a second “operator-only” path in this change.
- **[15 HTTPS handshakes drain battery / take too long]** → Early majority stop; 5s cap; tap-only so checks are user-initiated.
- **[Stale widget after Wi-Fi ↔ LTE]** → v1 is tap-only by design. The pipeline is ready for a later network-change trigger.
- **[No validated network vs captive portal]** → Captive / unvalidated networks follow the no-network path (no probes). A validated network that still fails every group shows three red cells, not “нет сети”.

## Migration Plan

First install of a new app: no data to migrate. Ship the bundled JSON in the APK. Rollback is uninstall or revert the app module.

## Open Questions

None that affect specs or the task breakdown. Hostname `www` vs apex for individual canaries can be settled while implementing the asset if a starter origin fails TLS.
