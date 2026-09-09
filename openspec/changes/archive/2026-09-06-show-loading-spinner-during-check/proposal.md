## Why

Tapping the widget starts a check that can take several seconds, but the home-screen UI only shows the text **Проверяем…** and can snap back to the last completed result before the pipeline finishes. The user should see a loading spinner for the whole wait, including the validated-network check, until a terminal result appears.

## What Changes

- Show an indeterminate loading spinner on the widget from tap until the check ends (no-network or three group verdicts).
- Keep that in-progress UI for the entire refresh, including the snapshot of validated-network availability and the address probes that follow.
- Do not restore the previous Ready or NoNetwork presentation while that refresh is still running.
- Keep the existing **Проверяем…** status line during in-progress; hide the spinner once a terminal result is shown.

Assumption: this change does **not** wait or retry for Android to mark the default network as validated. The pipeline still takes one snapshot; if that snapshot has no validated network, the result is still no-network.

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- `status-widget`: In-progress MUST be a visible loading spinner that stays up for the whole check, including network-availability determination, and MUST NOT be replaced by the previous completed result until the new result is ready.

## Impact

Widget layout (`widget_status.xml`), `WidgetPresenter` / `WidgetUi`, `WidgetBinder`, `StatusWidgetProvider`, and `WidgetStateStore` plus their unit tests. Probe rules, list source, and tap-only triggers are unchanged. No new permissions or libraries.
