## Why

On a phone it is hard to tell, at a glance, what the current connection can actually open: whitelist-only, ordinary unblocked sites, or even typically blocked sites. A home-screen widget that answers “what opens for me right now” on the active path (Wi‑Fi, LTE, or VPN as-is) removes the need to open a browser and guess.

## What Changes

- New Android app whose product surface is a home-screen widget with three verdict fields (whitelist, ordinary, blocked) plus a distinct no-network status.
- On tap, the app probes the current network path and updates each field to a single yes/no verdict (majority over an odd list of at least five addresses). A probe succeeds when any HTTP response arrives; timeouts and DNS/TLS/RST failures count as fail.
- Probe target lists are bundled in the app in a versioned data file behind a `ListSource` so a later change can load lists from the network without rewriting the widget or classifier.
- Refresh is a single pipeline with three intended triggers (tap, network change, periodic). This change wires tap only.
- Launcher icon opens a technical screen that explains how to add the widget. No settings or per-probe detail UI.
- No iOS client. The widget does not force a cellular network when Wi‑Fi is active. Provider block-page HTML is not classified in this change.

## Capabilities

### New Capabilities

- `status-widget`: Home-screen widget (three group verdicts, no-network, in-progress, last-check time, tap-to-refresh) and a technical “add the widget” activity.
- `access-probe`: Current-path HTTPS probes, HTTP-response success rule, majority verdict per group, and a trigger-agnostic refresh pipeline (tap in this change).
- `probe-lists`: Versioned bundled W/N/B address lists behind a replaceable source; odd count of at least five addresses per group.

### Modified Capabilities

- None. The repository has no existing main specs.

## Impact

Greenfield Android application (no existing app code). New home-screen widget, probe engine, bundled list asset, and a minimal launcher activity. Needs `INTERNET` and `ACCESS_NETWORK_STATE`. No backend, no remote list fetch, and no new server dependency in this change.
