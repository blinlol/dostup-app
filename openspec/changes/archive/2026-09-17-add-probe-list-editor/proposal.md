## Why

The three probe groups ship as a fixed bundled file, so a user who needs different canaries (rotten hosts, another region, their own sites) cannot change what the widget measures without a new APK. The app icon already exists and is the right place to edit those lists.

## What Changes

- **BREAKING**: The launcher activity is no longer instructions-only. Its main surface is an editor of the HTTPS URLs used for whitelist, ordinary, and blocked. Add-widget help moves behind a corner button (dialog). The copy that says the screen has no settings is removed.
- The user can add and remove URLs per group. Save persists a full replacement of all three groups only when each group has an odd count of at least five `https://` URLs with a host. Invalid drafts never become the lists used by checks.
- Bundled `probe_lists.json` stays the live default: checks use it until the user saves, and after a reset. App updates replace defaults for anyone who has not saved. Reset restores the **current** bundled lists, not a copy from first launch.
- After a successful Save, if at least one widget instance is placed, start a user-initiated check with the same in-progress presentation and preemption as a widget tap, using the newly saved lists. If no widget is placed, Save only persists lists and MUST NOT send probes.
- Tapping the widget still starts a check and MUST NOT open the launcher or the editor.
- Remote list download, per-probe diagnostics, and changing majority / timeout rules stay out of scope.

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- `probe-lists`: Effective groups MAY come from a user overlay; bundled lists remain the default and the reset target; Save is rejected unless each group still meets the odd-count-of-at-least-five HTTPS rule.
- `status-widget`: The launcher activity is the list editor plus a corner help control. The widget tap contract (refresh, do not open the app) is unchanged.
- `access-probe`: Saving valid lists MUST start the shared pipeline as a user-initiated check when a widget is placed, and MUST NOT start a check when none is placed.

## Impact

`MainActivity` and `activity_main` become the editor (three groups, Save, Reset, corner help). `ArchitectureGuardTest.launcherScreenHasInstructionsOnly` and the `status-widget` “no settings” requirement must be rewritten. List loading grows an overlay in front of `AssetListSource` / `JsonListSource` (likely SharedPreferences JSON with the existing schema); `RefreshPipeline` still reads only `ListSource`. Save with a widget present enqueues the existing unique `widget-refresh` work as a tap-equivalent. README must stop saying the site list is fixed. No new permissions, no backend, no change to `ProbeEngine` majority or timeouts.
