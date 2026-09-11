## 1. Trigger-aware refresh work

- [x] 1.1 Add a shared enqueue helper that writes `trigger` (`tap` | `network` | `periodic`) onto unique one-time `RefreshWorker` work named `widget-refresh`, using `REPLACE` for tap, `REPLACE` for network unless `stateStore.load()` is `Checking` (then skip), and `KEEP` for periodic, and verify a unit or source test covers those three policies including the Checking skip
- [x] 1.2 Change tap handling so it still `save(Checking)` then `updateAll(Checking)` before enqueue, and change `RefreshWorker.doWork` so it paints `Checking` only when `trigger` is `tap`, then always `pipeline.refresh()`, `save`, and paint the terminal state, and verify `WidgetStateStoreTest` asserts tap paints Checking before refresh while network/periodic workers do not call `updateAll(Checking)`

## 2. Periodic backstop and widget lifetime

- [x] 2.1 Add a thin periodic worker that only enqueues the one-time job with trigger `periodic` and `KEEP`, schedule it as unique `periodic-refresh` every 15 minutes with `ExistingPeriodicWorkPolicy.KEEP` from `onEnabled`/`onUpdate` when widgets exist, and verify the request uses `PeriodicWorkRequest` with a 15-minute interval and that unique name
- [x] 2.2 Cancel `periodic-refresh` and stop automatic enqueue in `onDisabled`, and verify `onDisabled` cancels that unique periodic work

## 3. Default-path callback

- [x] 3.1 Register `registerDefaultNetworkCallback` when at least one widget exists (`onEnabled` and `WlApp.onCreate`), unregister in `onDisabled`, debounce ~2s on `onAvailable`/`onLost`/default-network switch, then enqueue trigger `network`, and verify the callback is registered only while widgets exist and that a burst of callbacks results in a single enqueue
- [x] 3.2 Keep the launcher free of interval settings and keep `RefreshPipeline` as the only `refresh()` entry, and verify `ArchitectureGuardTest.launcherScreenHasInstructionsOnly` still passes and probe tests still cover no-network skip versus three group verdicts

## 4. Guards and wrap-up

- [x] 4.1 Replace `noPeriodicOrNetworkCallbackTriggers` (and the matching `WidgetStateStoreTest` source bans) with assertions that `PeriodicWorkRequest`, unique `periodic-refresh`, and `registerDefaultNetworkCallback` exist, and that the manifest still has no `CONNECTIVITY_CHANGE`, and verify those tests pass
- [x] 4.2 Run `./gradlew test assembleDebug` and a manual pass: tap still shows spinner then a timed result; airplane / Wi‑Fi toggle updates without spinner or in-progress gray; removing the last widget stops automatic checks, and verify each matches the status-widget and access-probe scenarios in this change
