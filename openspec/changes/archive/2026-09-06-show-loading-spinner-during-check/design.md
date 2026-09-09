## Context

See `proposal.md` for motivation and `specs/status-widget/spec.md` for the in-progress contract.

Today a tap calls `WidgetBinder.updateAll(Checking)` and enqueues `RefreshWorker`. `WidgetStateStore.save(Checking)` is a no-op, so `onUpdate` reloads the last Ready or NoNetwork and can replace the in-progress UI while the worker is still running. The layout has no `ProgressBar`; in-progress is only the status text **Проверяем…**. `RefreshPipeline` still takes a single `hasValidatedDefault()` snapshot, then either returns NoNetwork or runs probes. Architecture tests forbid `NetworkCallback` and periodic work.

## Goals / Non-Goals

**Goals:**

- Add an indeterminate spinner to the widget RemoteViews and bind it from `WidgetState.Checking`.
- Keep last completed result in storage while a check is in flight, but present Checking for every redraw until the worker writes a terminal state.
- Cover the whole refresh with that presentation: the validated-network snapshot and the probes.

**Non-Goals:**

- Waiting or retrying until Android sets `NET_CAPABILITY_VALIDATED`.
- Per-cell spinners, incremental group updates, or changing probe/majority rules.
- New triggers, permissions, or UI libraries.

## Decisions

### 1. Persist an in-flight flag next to the last completed result

**Choice:** Extend `WidgetStateStore` so `save(Checking)` sets a persisted `checking` flag without clearing the last Ready/NoNetwork payload. `load()` returns `WidgetState.Checking` while that flag is set. `save(Ready)` and `save(NoNetwork)` write the new result and clear the flag.

**Why:** The spec requires a redraw during a running check to keep the spinner, including after the process is recreated. An in-memory-only flag is lost when WorkManager starts a fresh process; replacing the last result with Checking would leave a stuck spinner if the worker never completes.

**Alternatives:** In-memory flag on `AppContainer` (fails process recreate); persist Checking as the only stored state (stuck spinner after a killed worker); query WorkManager unique work (couples presentation to WorkManager and is harder to unit-test).

```
  [Tap]
    |
    v
  save(Checking) + updateAll(Checking)     last Ready/NoNetwork kept
    |
    v
  RefreshWorker
    |-- updateAll(Checking)                recover after process start
    |-- hasValidatedDefault snapshot
    |     +-- false --> save(NoNetwork) --> hide spinner
    |     +-- true  --> probes --> save(Ready) --> hide spinner
    v
  onUpdate always presents store.load()    Checking while flag is set
```

`RefreshWorker` MUST paint Checking again at the start of `doWork` so a new process does not sit on stale RemoteViews until probes finish.

### 2. One small ProgressBar in the status row

**Choice:** Add an indeterminate `ProgressBar` beside the existing status `TextView`. `WidgetUi` gains `showSpinner`. Presenter sets it true only for `Checking`. Binder toggles `VISIBLE`/`GONE`. Keep **Проверяем…**. Cells stay idle gray during Checking, as they do now.

**Why:** App Widgets already support this via `RemoteViews.setViewVisibility`. One spinner matches “the check is running” without implying per-group progress the pipeline does not emit.

**Alternatives:** Spinner replacing the whole widget (hides the three labels); a spinner in each cell (implies incremental verdicts); text-only (does not meet “колесо загрузки”).

### 3. Network availability stays a one-shot snapshot

**Choice:** Leave `RefreshPipeline`’s `hasValidatedDefault()` call as a single read. The spinner is already showing from tap, so that read and the probes both happen under Checking.

**Why:** Waiting for `VALIDATED` would change no-network timing and would need polling or a `NetworkCallback`, which architecture tests forbid. The proposal explicitly keeps the snapshot.

**Alternatives:** Poll until validated or timeout (different product: delayed “Нет сети”); register a default network callback (blocked by current architecture guards).

## Risks / Trade-offs

- **[Stuck spinner if the worker never writes a terminal state]** → Clear the flag in the worker after `refresh()` returns, including no-network. Keep last completed payload so a later `onUpdate` can show it if the flag is cleared without a new result. Unique work `REPLACE` still runs one worker to completion.
- **[Store shape change]** → Treat a missing `checking` key as false so existing prefs keep showing the last completed result.
- **[Existing store test expects save(Checking) to be a no-op]** → Replace that assertion with: after `save(Ready)` then `save(Checking)`, `load()` is Checking; after a following `save(Ready|NoNetwork)`, `load()` is that terminal state and the previous in-progress presentation is gone.

## Migration Plan

No user-facing migration. First launch after the update reads old JSON without `checking` as not in-progress. Rollback is reverting the widget module; leftover `checking: true` in prefs would only matter if that build is rolled back while a check is running, which is acceptable.

## Open Questions

None. Spinner placement (status row) and the in-flight flag are settled here and do not change the spec.
