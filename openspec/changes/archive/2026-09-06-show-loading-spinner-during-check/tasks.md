## 1. Persist in-flight checking

- [x] 1.1 Extend `WidgetStateStore` so `save(Checking)` sets a `checking` flag without clearing the last Ready/NoNetwork payload, `load()` returns `Checking` while that flag is set, and a missing flag reads as false, and verify `WidgetStateStoreTest` covers Ready-then-Checking-then-terminal plus JSON without `checking`
- [x] 1.2 On tap, `save(Checking)` before `updateAll(Checking)`, and verify `StatusWidgetProvider` persists Checking so a later `onUpdate` that calls `load()` still presents in-progress rather than the previous completed result

## 2. Spinner on the widget

- [x] 2.1 Add an indeterminate `ProgressBar` beside the status line in `widget_status.xml` (default gone), and verify the layout contains that view next to `status_line`
- [x] 2.2 Add `showSpinner` to `WidgetUi`, set it true only for `Checking`, bind visibility in `WidgetBinder`, and keep **Проверяем…** with idle cells, and verify `WidgetPresenterTest` asserts spinner on Checking and hidden for Ready, NoNetwork, and Idle

## 3. Keep the spinner until the check ends

- [x] 3.1 At the start of `RefreshWorker.doWork`, `updateAll(Checking)` before `pipeline.refresh()`, then `save` and paint the terminal state (Ready or NoNetwork), and verify the worker source paints Checking first and writes a terminal state afterward
- [x] 3.2 Leave `RefreshPipeline` as a single `hasValidatedDefault()` snapshot with no `NetworkCallback` or wait loop, and verify `ProbeEngineTest` still covers no-network skip versus three group verdicts and `ArchitectureGuardTest` still forbids network callbacks and periodic work
- [x] 3.3 Run `./gradlew test assembleDebug` and a tap-refresh pass, and verify the spinner stays visible through the network-availability snapshot until no-network or the three verdicts appear, then hides with the result time
