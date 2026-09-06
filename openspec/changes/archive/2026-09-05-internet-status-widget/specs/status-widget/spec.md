## Purpose

Shows on the Android home screen whether whitelist, ordinary, and typically blocked sites open on the phone's current connection, plus a distinct no-network state.

## ADDED Requirements

### Requirement: Three verdict fields

The home-screen widget MUST display exactly three fields, one for each probe group: whitelist, ordinary, and blocked. Each field MUST show a single verdict for its group (available or not available) and MUST NOT show a per-address fraction.

#### Scenario: All groups available

- **WHEN** the latest check reports available for whitelist, ordinary, and blocked
- **THEN** the widget shows an available verdict in all three fields

#### Scenario: Only whitelist available

- **WHEN** the latest check reports available for whitelist and not available for ordinary and blocked
- **THEN** the widget shows available only in the whitelist field

#### Scenario: Ordinary internet without blocked sites

- **WHEN** the latest check reports available for whitelist and ordinary and not available for blocked
- **THEN** the widget shows available in the whitelist and ordinary fields and not available in the blocked field

### Requirement: No-network status is distinct from failed probes

When the device has no validated network, the widget MUST show a no-network status and MUST NOT present three not-available verdicts as if a check had completed.

#### Scenario: Airplane mode or no validated network

- **WHEN** the user triggers a refresh and the device has no validated network
- **THEN** the widget shows a no-network status and the three fields are not shown as completed not-available verdicts

#### Scenario: Network present but every group fails

- **WHEN** a check runs on a validated network and every group verdict is not available
- **THEN** the widget does not show no-network and instead shows not-available in all three fields

### Requirement: In-progress and last-check time

While a check is running, the widget MUST indicate that a check is in progress. After a check finishes (including no-network), the widget MUST show when that result was produced.

#### Scenario: Tap starts a visible check

- **WHEN** the user taps the widget and a check begins
- **THEN** the widget shows an in-progress indication before the new verdicts appear

#### Scenario: Completed check shows its time

- **WHEN** a check completes successfully or with no-network
- **THEN** the widget displays the time of that result

### Requirement: Tap refreshes the widget

Tapping the widget MUST start a new check of the current connection. Tapping MUST NOT open the launcher activity or a settings screen.

#### Scenario: Tap on the home screen

- **WHEN** the user taps the widget
- **THEN** a new check starts and the launcher activity does not open

### Requirement: Technical launcher screen

Opening the app from the launcher icon MUST show a technical screen that explains how to add the widget to the home screen. That screen MUST NOT offer settings, list editing, or per-probe details.

#### Scenario: Open from the app drawer

- **WHEN** the user opens the app from the launcher icon
- **THEN** they see instructions for adding the widget and no settings or detail controls
