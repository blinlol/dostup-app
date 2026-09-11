## Context

See `proposal.md` for motivation and the delta specs for the trigger and presentation contracts.

Today only tap starts a check: `StatusWidgetProvider` saves `Checking`, paints it, and enqueues unique one-time `RefreshWorker` (`tap-refresh`, `REPLACE`). The worker always paints `Checking` again, then `RefreshPipeline.refresh()`, then saves and paints the terminal state. `ConnectivityManager` is read once for `NET_CAPABILITY_VALIDATED`; there is no callback. `updatePeriodMillis` is 0. Architecture tests forbid `PeriodicWorkRequest`, `registerNetworkCallback`, `registerDefaultNetworkCallback`, and `CONNECTIVITY_CHANGE`. WorkManager is already a dependency.

`RefreshPipeline` stays the only classification entry. This change adds triggers and a silent-versus-tap presentation split around that entry.

## Goals / Non-Goals

**Goals:**

- Attach a default-path `NetworkCallback` and a 15-minute WorkManager periodic job to the same unique one-time refresh work that tap uses.
- Pass the trigger into that work so only tap writes `Checking` and paints the spinner.
- Encode the preemption table in enqueue policy plus the persisted `checking` flag.
- Start automatic triggers when a widget exists (`onEnabled` / process start) and tear them down in `onDisabled`.

**Non-Goals:**

- Changing probe timeouts, majority, or `hasValidatedDefault()` snapshot semantics.
- A settings UI or a period other than 15 minutes.
- `AlarmManager`, `updatePeriodMillis`, deprecated `CONNECTIVITY_CHANGE`, or a foreground service to keep the process alive.
- Waiting for `VALIDATED` instead of the existing one-shot snapshot.

## Decisions

### 1. Shared one-time work plus a thin periodic wrapper

**Choice:** Keep one unique one-time `RefreshWorker` (rename unique name to `widget-refresh`) as the only job that calls `pipeline.refresh()`. Schedule `PeriodicWorkRequest` unique name `periodic-refresh`, interval 15 minutes, `ExistingPeriodicWorkPolicy.KEEP`. That periodic worker only enqueues the one-time job with trigger `periodic` and `KEEP`.

**Why:** WorkManager's minimum periodic interval is 15 minutes and it reschedules across process death and reboot. If the periodic request *were* `RefreshWorker` under a different unique name, it could run in parallel with a tap. A wrapper that joins the same unique one-time work avoids two probe storms.

**Alternatives:** `AppWidgetProvider.updatePeriodMillis` (30 minute floor, launcher may skip); `AlarmManager` (Doze, OEM killers, more code); chaining one-time work with a 15-minute delay (easy to drop after a kill).

### 2. Default-network callback, not CONNECTIVITY_CHANGE

**Choice:** `ConnectivityManager.registerDefaultNetworkCallback` while widgets exist. Treat `onAvailable`, `onLost`, and default-network switches as path changes. Debounce ~2 seconds, then enqueue the one-time job with trigger `network`. Unregister in `onDisabled`. Re-register from `Application.onCreate` when `AppWidgetManager` reports at least one widget id so a process woken by the periodic job listens again.

**Why:** This matches “active path changed” (Wi‑Fi / LTE / VPN / none) on minSdk 26. Manifest `CONNECTIVITY_CHANGE` is not delivered to apps as of modern Android. `registerNetworkCallback(NetworkRequest, PendingIntent)` wakes on *available* more than on *lost*; the 15-minute job is the backstop for a dead process.

**Alternatives:** PendingIntent callback only (misses loss unless combined with something else); listening only while a check runs (misses the whole point).

First `onAvailable` after register typically fires immediately, so placing a widget starts a silent check. That is intended.

### 3. Trigger on the work request; Checking only for tap

**Choice:** Put a `trigger` string on `RefreshWorker` input (`tap` | `network` | `periodic`). Tap path: `save(Checking)`, `updateAll(Checking)`, enqueue `REPLACE`. Auto path: do not `save(Checking)` and do not `updateAll(Checking)`. The worker paints `Checking` at the start of `doWork` only for `tap`; every trigger still `save`s and paints the terminal Ready/NoNetwork.

**Why:** `onUpdate` already presents `store.load()`. Leaving the `checking` flag unset keeps last Ready/NoNetwork (or Idle) on screen during auto, including after a process recreate. Reusing `Checking` for auto would violate the silent-auto spec.

**Alternatives:** A new `WidgetState.SilentChecking` (presenter complexity, store shape change); querying WorkManager for running work in the presenter (harder to unit-test).

### 4. Preemption via unique work policy and the checking flag

```
  Tap / NetworkCallback / Periodic wrapper
              |
              v
        enqueue widget-refresh
              |
    +---------+---------+
    |                   |
    tap: REPLACE        auto:
    save+paint          if load() is Checking -> skip
    Checking            network: REPLACE (silent)
                        periodic: KEEP (silent)
              |
              v
        RefreshWorker
          tap     -> paint Checking -> pipeline -> terminal
          auto    -> pipeline -> terminal
```

**Choice:**

| Incoming | Policy |
|---|---|
| tap | `REPLACE`, write Checking |
| network, and store is Checking | skip |
| network, otherwise | `REPLACE`, do not write Checking |
| periodic | `KEEP`, do not write Checking |

**Why:** Unique `REPLACE` already cancels the previous worker, which is what tap and a new path need. The persisted Checking flag is the only durable “tap is in flight” signal after process recreate; auto must not `REPLACE` that. Periodic `KEEP` matches “do not queue.” Debounce on the callback collapses Wi‑Fi+VPN bursts before enqueue.

**Alternatives:** Always `REPLACE` (a network flap would cancel a tap and drop the spinner); always `KEEP` (a path change would finish probes on the old network).

### 5. Invert architecture guards, keep CONNECTIVITY_CHANGE banned

**Choice:** Replace `noPeriodicOrNetworkCallbackTriggers` with assertions that periodic unique work and `registerDefaultNetworkCallback` exist, widgets still do not read list files, and the manifest still has no `CONNECTIVITY_CHANGE`. Point `WidgetStateStoreTest` source guards at trigger-aware enqueue/`doWork` instead of “never Periodic / never callback.”

**Why:** Those tests encoded the v1 tap-only non-goal. Leaving them would fail the first compile of the intended design.

## Risks / Trade-offs

- **[Process dead during a path change]** → Periodic 15-minute job wakes the process and re-registers the callback. Widget can be stale for up to one period.
- **[Doze / OEM deferral of periodic work]** → Interval is a floor, not a wall-clock guarantee. Tap remains the immediate path. Do not fight Doze with a foreground service.
- **[Network burst / VPN flap]** → ~2 s debounce plus network `REPLACE` of silent work. Tap still wins via Checking.
- **[Worker cancellation leaves in-flight HTTPS]** → Accept leftover sockets until timeout; next `refresh()` is a new snapshot. No change to `ProbeEngine` cancellation in this design.
- **[Silent auto then tap]** → Tap `REPLACE` plus Checking overlay is enough; no extra state machine.
- **[Metered cellular data]** → Same probe budget as tap, now also on a timer and path changes. Early majority stop already limits this; interval is 15 minutes by product choice.

## Migration Plan

No stored-state migration. Existing prefs without `checking` still load as the last completed result. After upgrade, `onEnabled`/`onUpdate`/`Application.onCreate` schedule unique periodic work with `KEEP` (safe if already present) and register the callback when widgets exist. Rollback is reverting the app module; leftover periodic work is cancelled in `onDisabled` or by uninstall.

## Open Questions

None. Interval, silent auto, path-change meaning, and preemption are in the proposal and specs. Debounce duration (~2 s) can move slightly in implementation without changing those.
