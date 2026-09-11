## Why

The widget only refreshes when the user taps it, so after Wi‑Fi ↔ LTE, VPN, or a few minutes of idle time the three cells describe a path that may no longer be active. The check pipeline is already trigger-agnostic; it needs the two remaining triggers (network-path change and a 15-minute timer) so the home-screen answer stays current without a tap.

## What Changes

- Start a check when the device's default network path changes (Wi‑Fi, cellular, VPN, or loss of a validated default network).
- Start a check on a 15-minute timer while at least one widget instance is placed, as a backstop when the process is dead and a path change was missed.
- Automatic checks (timer and network-path change) MUST keep the last completed presentation: no loading spinner and no gray in-progress cells until the new Ready or NoNetwork result is painted.
- Tap checks keep the existing in-progress UI (spinner, **Проверяем…**, idle-gray cells).
- Tap always replaces an in-flight automatic check. An automatic check MUST NOT interrupt a tap. A network-path change MAY replace an in-flight automatic check. A timer tick MUST NOT queue behind or cancel a running check.
- Register and unregister automatic triggers with widget lifetime (`onEnabled` / `onDisabled`). No interval settings UI; the launcher screen stays instructions-only.
- Verdict rules, list source, and tap-to-refresh (without opening the launcher) stay the same.

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- `access-probe`: The shared pipeline MUST accept tap, default-network-path change, and a 15-minute periodic trigger, all without changing verdict or no-network rules.
- `status-widget`: In-progress spinner and gray cells apply only to tap-started checks. Automatic checks MUST leave the previous completed (or Idle) presentation visible until the new result arrives.

## Impact

`StatusWidgetProvider` (enable/disable, trigger dispatch), `RefreshWorker` (trigger-aware unique work and whether to paint `Checking`), architecture tests that currently forbid `PeriodicWorkRequest` and `NetworkCallback`, and the two specs above. `RefreshPipeline`, probe engine, lists, and launcher activity stay as they are. No new permissions; `ACCESS_NETWORK_STATE` is already present. No settings screen and no change to probe timeouts or majority rules.
