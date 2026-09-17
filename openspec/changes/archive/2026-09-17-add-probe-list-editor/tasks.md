## 1. Overlay source and validation

- [x] 1.1 Extend `ProbeGroups.validate()` so addresses are trimmed, must be `https://` URLs with a host, and must be unique within a group (keep odd count ≥ 5), and verify unit tests refuse `http://`, empty host, in-group duplicates, even size, and size below five while still accepting the bundled asset
- [x] 1.2 Add an overlay `ListSource` that reads a JSON string store (same schema as `probe_lists.json`), uses `JsonListSource` when that string is valid, otherwise `AssetListSource`, with write-after-validate and clear, and verify tests for missing/empty/corrupt/invalid overlay → bundled groups, valid overlay → saved groups, and clear → bundled again
- [x] 1.3 Wire `AppContainer` so `RefreshPipeline` receives the overlay source (not a raw `AssetListSource`) and verify `ArchitectureGuardTest.widgetAndProbeDoNotReadListFiles` still passes

## 2. Launcher editor

- [x] 2.1 Replace `activity_main` / `MainActivity` with a scrollable three-group URL editor (add/remove rows), Save, Reset, and a corner help control, using Russian strings and removing «нет настроек», and verify the layout contains those controls and the help dialog text still explains how to add the widget
- [x] 2.2 Load `listSource.groups()` into the editor on open, persist all three groups together only when validation passes, leave `ListSource` unchanged while the draft is invalid, and on Reset clear the overlay and refill the fields from bundled groups, and verify tests that an invalid draft does not change `groups()` while a valid save then reset round-trips overlay → bundled
- [x] 2.3 Rewrite `ArchitectureGuardTest.launcherScreenHasInstructionsOnly` to assert the editor + Save/Reset + corner help and that `WidgetBinder` still does not reference `MainActivity`, and verify that test passes

## 3. Save-triggered check

- [x] 3.1 After a successful overlay write, if `AutoRefreshController.widgetsExist` then `save(Checking)`, `updateAll(Checking)`, and `RefreshScheduler.enqueue(TAP)`, otherwise persist only, and verify a unit or source test covers both branches and that `MainActivity` does not call `pipeline.refresh()` itself
- [x] 3.2 Keep `RefreshPipeline` as the only `refresh()` entry and reuse `RefreshTrigger.TAP` (no new trigger enum), and verify `ArchitectureGuardTest.refreshPipelineIsTheOnlyRefreshEntry` and `RefreshSchedulerTest.tapAlwaysReplaces` still pass

## 4. Docs and wrap-up

- [x] 4.1 Update the root README so it no longer says the site list is fixed and states that defaults ship in the app and can be edited and reset from the app icon, and verify that wording is in the file
- [x] 4.2 Run `./gradlew test assembleDebug` and a manual pass: editor shows bundled URLs; invalid Save does not change widget checks; valid Save with a widget shows the tap spinner then new verdicts; Save with no widget does not probe; Reset restores bundled URLs; widget tap still does not open the app, and verify each matches the probe-lists, status-widget, and access-probe scenarios in this change
