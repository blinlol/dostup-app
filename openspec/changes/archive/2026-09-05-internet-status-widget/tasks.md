## 1. Android skeleton

- [x] 1.1 Create a single Kotlin Android app module (minSdk 26, `INTERNET` and `ACCESS_NETWORK_STATE`) and verify `./gradlew assembleDebug` succeeds
- [x] 1.2 Add an empty `AppWidgetProvider` plus a placeholder launcher activity and verify the debug APK installs and shows the app icon

## 2. Probe lists

- [x] 2.1 Add the versioned bundled JSON with `whitelist`, `ordinary`, and `blocked` groups using the starter origins from design.md and verify each group has an odd count of at least five HTTPS URLs
- [x] 2.2 Implement `ListSource` that reads only that asset and verify a unit test fails if a group is missing, even-sized, or smaller than five
- [x] 2.3 Keep widget and probe code dependent on `ListSource` only (no direct asset reads) and verify a search of those packages shows no raw list-file I/O

## 3. Check pipeline

- [x] 3.1 Implement HTTPS GET probe with a 5s timeout where any HTTP status is success and DNS/TLS/timeout/RST are failure, and verify unit tests cover a status response versus a timeout
- [x] 3.2 Implement majority verdict with early stop (`floor(n/2)+1`) and verify tests for 3/5 success, 3/5 failure, and cancelled remainder
- [x] 3.3 Implement `RefreshPipeline` that skips probes when there is no validated default network and otherwise uses the default path as-is, and verify tests for no-network versus three group verdicts
- [x] 3.4 Expose a single pipeline entry used by triggers and wire only a tap trigger, and verify no network-callback or periodic worker is registered in the manifest or application code

## 4. Status widget

- [x] 4.1 Build a ~4×1 RemoteViews layout with three labeled cells (Белый / Обычный / Запрет) and a status line, and verify the layout renders three equal fields in the widget preview
- [x] 4.2 Map `WidgetState` to the layout: green/red verdicts, gray in-progress, distinct «Нет сети» (not three red cells), «Проверяем…», and `HH:mm` time, and verify a unit or screenshot test for each of those states
- [x] 4.3 Persist the last `WidgetState` and restore it when the widget is created, and verify a process-recreate still shows the last completed result
- [x] 4.4 Bind the whole widget to a tap that starts the pipeline (not the launcher activity), shows in-progress immediately, then writes the new state, and verify tap does not open the technical screen

## 5. Technical screen and wrap-up

- [x] 5.1 Implement the launcher activity with add-widget instructions only (no settings or probe details) and verify opening the app icon shows that screen
- [x] 5.2 Run `./gradlew test assembleDebug` and a manual pass: no network, whitelist-only pattern, ordinary pattern, all-available pattern, and tap refresh, and verify each matches the status-widget and access-probe scenarios
