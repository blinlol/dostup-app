## Context

See `proposal.md` for motivation and the delta specs for overlay, launcher, and save-triggered check contracts.

Today `AppContainer` wires `AssetListSource` → `JsonListSource` as the only `ListSource`. `RefreshPipeline.refresh()` snapshots `lists.groups()` once. `MainActivity` inflates `activity_main` (instructions `TextView` only). `ArchitectureGuardTest.launcherScreenHasInstructionsOnly` forbids `settings` in the activity and requires the string «нет настроек». Widget tap already `save(Checking)`, `updateAll(Checking)`, and `RefreshScheduler.enqueue(..., TAP)` with `REPLACE`. `AutoRefreshController.widgetsExist()` is the existing “at least one instance” check. Widget state already uses SharedPreferences (`widget_state`); lists do not.

`ListSource` stays the only read path for probes. This change adds a persisted overlay, a launcher editor, and a save → tap-equivalent enqueue when a widget exists.

## Goals / Non-Goals

**Goals:**

- Persist a full three-group overlay in the same JSON shape as `probe_lists.json`, falling back to the asset when the overlay is absent or invalid.
- Keep write/reset off `ListSource`; probes keep calling `groups()` only.
- Replace the launcher layout with a scrollable three-group editor, Save, Reset, and a corner help dialog.
- On valid Save with `widgetsExist()`, reuse the tap enqueue path (Checking + `RefreshTrigger.TAP`).

**Non-Goals:**

- A new `RefreshTrigger` value, Compose, Jetpack Preferences, remote list fetch, or changing `ProbeEngine` / timeouts / majority.
- Per-URL probe results on the editor or the widget.
- Copy-on-first-launch of bundled lists into storage.

## Decisions

### 1. Overlay in a separate prefs file, same JSON schema

**Choice:** Store the overlay as one JSON string (`version` + `groups`) in SharedPreferences named separately from `widget_state` (e.g. `probe_lists` / key `overlay`). Parse with existing `JsonListSource`. If the key is missing, empty, or `groups()` throws, return bundled `AssetListSource` groups.

**Why:** Matches the bundled file, keeps `JsonListSource` as the schema gate, and leaves widget state prefs untouched. Unsaved devices keep getting APK updates because no overlay key exists.

**Alternatives:** Copy asset into prefs on first launch (stale after APK updates); DataStore (new dependency); per-group string sets (weaker validation, different shape).

```
  RefreshPipeline.lists.groups()
              |
              v
       OverlayListSource
              |
     +--------+--------+
     | valid overlay   | missing / corrupt / invalid
     v                 v
  JsonListSource    AssetListSource
  (prefs JSON)      (probe_lists.json)
```

A small store type (read/write/clear string) mirrors `StringStore` used by `WidgetStateStore`. `AppContainer` builds overlay + asset and injects the overlay source into `RefreshPipeline`. Reset is `clear()`, then the editor reloads `groups()`.

### 2. Validate on write; drafts stay in the activity

**Choice:** Extend `ProbeGroups.validate()` (or a function used by both parse and Save) to require unique trimmed `https://` URLs with a host, plus the existing odd-count ≥ 5 rule. Save writes only after validate succeeds. The activity holds the draft; `ListSource` never sees it.

**Why:** `RefreshPipeline` already snapshots `groups()` at the start of a check, so an in-flight check cannot pick up a half-edited list. Corrupt prefs fall back in the overlay source so a bad write or partial edit cannot crash `refresh()`.

**Alternatives:** Autosave each keystroke (would need to allow invalid persisted state); disable rows below five (blocks the user from deleting toward a rewrite).

Trim before validate. Duplicate comparison is on the trimmed string. Paths are allowed (`https://example.com/health`). The same URL in two groups is allowed.

Save is disabled (or a no-op that does not write) while invalid.

### 3. XML editor on MainActivity, help as a dialog

**Choice:** Stay on AppCompat + XML. ScrollView with three labeled URL lists (add row / remove row), Save, Reset. A corner `ImageButton` / similar opens an `AlertDialog` with the existing add-widget copy, minus «нет настроек». Russian strings.

**Why:** The module has no Compose or Preference library. A dialog avoids a second activity. The widget tap contract is unchanged (`WidgetBinder` still targets `StatusWidgetProvider`).

**Alternatives:** PreferenceScreen (awkward for dynamic URL lists); a second Settings activity (extra navigation the user did not want); putting help as the first screen (rejected in exploration).

Rewrite `launcherScreenHasInstructionsOnly` so it asserts the editor layout, Save/Reset, corner help, and that `MainActivity` still does not appear in `WidgetBinder`.

### 4. Save enqueues TAP when a widget exists; otherwise persist only

**Choice:** After a successful overlay write, if `AutoRefreshController.widgetsExist(context)` then the same sequence as widget tap: `stateStore.save(Checking)`, `WidgetBinder.updateAll(Checking)`, `RefreshScheduler.enqueue(TAP)`. If no widget, return after persist.

**Why:** TAP already means user-initiated: spinner, `REPLACE`, preemption vs automatic. A fourth enum value would duplicate that table. No widget → no probes matches automatic-trigger lifetime and avoids HTTPS from the activity alone.

**Alternatives:** `RefreshTrigger.SAVE` with identical policy (more code, same behavior); always probe (rejected); silent automatic enqueue (would leave stale cells on screen and skip Checking).

```
  Save valid overlay
          |
          +-- !widgetsExist --> stop
          |
          v
  save+paint Checking
          |
          v
  enqueue widget-refresh TAP (REPLACE)
          |
          v
  RefreshWorker: paint Checking -> pipeline.groups() = overlay -> terminal
```

Extracting the tap sequence into a helper used by `StatusWidgetProvider` and `MainActivity` keeps the two call sites from drifting; not required if both stay three lines.

### 5. README tells the truth about lists

**Choice:** Replace «фиксированному списку сайтов» with a short note that defaults ship in the app and can be edited (and reset) from the app icon. Keep the README a phone-user install guide (`public-github` does not need a delta).

**Why:** That sentence would be false after this change. Specs for public GitHub only require the README to say what the product does.

## Risks / Trade-offs

- **[First-run users miss add-widget help]** → Corner control plus README / dialog text; do not hide the control behind an overflow-only menu.
- **[Saved overlay never picks up new bundled canaries]** → Intended. Reset is the escape hatch. Document it on the editor (Reset label).
- **[Corrupt overlay]** → Overlay source catches parse/validate failure and uses the asset. Next successful Save overwrites the bad key.
- **[Save during an in-flight tap]** → TAP `REPLACE` cancels the old worker; leftover sockets until timeout already accepted for tap-vs-auto. New `groups()` snapshot uses the overlay.
- **[Large user lists]** → Majority early-stop still caps work at `floor(n/2)+1` per group. No max in this change; revisit if needed.
- **[Block-page / wrong-role URLs]** → User-owned lists; verdicts are personal. Do not add body heuristics.

## Migration Plan

Existing installs have no overlay key: behavior stays bundled lists. After upgrade, opening the editor shows current asset groups. Rollback is reverting the app module; an leftover `probe_lists` prefs file is ignored by old `AssetListSource`-only code. Uninstall clears prefs.

## Open Questions

None. Overlay vs copy-on-launch, Save → TAP, no probes without a widget, and corner-dialog help are fixed in the proposal and specs.
