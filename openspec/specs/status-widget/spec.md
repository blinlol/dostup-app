# status-widget Specification

## Purpose

Shows on the Android home screen whether whitelist, ordinary, and typically blocked sites open on the phone's current connection, plus a distinct no-network state.

## Requirements

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

A tap-started check MUST show an indeterminate loading spinner and idle-gray cells from the moment it begins until it finishes, including while the system determines whether the device has a validated network. During a tap-started check the widget MUST NOT show the previous completed result as if the check had already finished. An automatic check (periodic or default-path change) MUST keep the last completed presentation, or Idle if there is no completed result: no loading spinner and no switch of the three fields to in-progress gray while that check is running. After any check finishes (including no-network), the widget MUST hide the spinner if it was showing and MUST show when that result was produced.

#### Scenario: Tap starts a visible check

- **WHEN** the user taps the widget and a check begins
- **THEN** the widget shows a loading spinner and an in-progress status before the new verdicts or no-network status appear

#### Scenario: Spinner stays through network-availability determination

- **WHEN** a tap-started check is running and the system is determining whether the device has a validated network
- **THEN** the loading spinner remains visible until that determination has produced a terminal result (no-network or group verdicts)

#### Scenario: Previous result is not restored during a check

- **WHEN** a tap-started check is still running and the widget is redrawn
- **THEN** the widget still shows the loading spinner and does not show the previous completed verdicts or no-network status as if the check had already finished

#### Scenario: Automatic check keeps the last presentation

- **WHEN** a periodic or default-path-change check is running and a previous completed result exists
- **THEN** the widget keeps that completed presentation with the spinner hidden and does not switch the three fields to in-progress gray

#### Scenario: Automatic check from Idle stays Idle until done

- **WHEN** a periodic or default-path-change check is running and there is no completed result yet
- **THEN** the widget stays on Idle (no spinner) until the new verdicts or no-network status appear

#### Scenario: Automatic check redraw keeps the last presentation

- **WHEN** an automatic check is still running and the widget is redrawn
- **THEN** the widget still shows the last completed result or Idle, not the tap in-progress presentation

#### Scenario: Completed check shows its time

- **WHEN** a check completes successfully or with no-network
- **THEN** the widget hides the loading spinner and displays the time of that result

### Requirement: Tap refreshes the widget

Tapping the widget MUST start a new check of the current connection. Tapping MUST NOT open the launcher activity or a settings screen.

#### Scenario: Tap on the home screen

- **WHEN** the user taps the widget
- **THEN** a new check starts and the launcher activity does not open

### Requirement: Technical launcher screen

Opening the app from the launcher icon MUST show a screen whose main surface is an editor of the three probe groups (whitelist, ordinary, and blocked): the current effective addresses, controls to add and remove URLs, a save control, and a reset control that restores the bundled defaults. That screen MUST NOT show per-probe diagnostics or per-address fractions. How to add the widget MUST remain available from a control in a corner of the screen and MUST NOT be the only content of the screen.

#### Scenario: Open from the app drawer

- **WHEN** the user opens the app from the launcher icon
- **THEN** they see the three group lists with the current effective addresses and save and reset controls, and they do not see per-probe details

#### Scenario: Help stays reachable from a corner

- **WHEN** the user opens the app and activates the corner help control
- **THEN** they see instructions for adding the widget to the home screen

#### Scenario: Editor shows bundled defaults before any save

- **WHEN** the user opens the app and has never successfully saved lists
- **THEN** the three groups show the bundled default addresses

#### Scenario: Reset fills the editor with bundled defaults

- **WHEN** the user activates reset after having saved lists
- **THEN** the three groups show the bundled default addresses currently shipped in the app

### Requirement: Save control stays off until lists are valid

The save control MUST NOT persist lists while any group fails the odd-count-of-at-least-five rule or the HTTPS URL rules. The user MUST be able to edit toward a valid set without those in-progress values becoming the widget's probe targets.

#### Scenario: Save does nothing while a group is too small

- **WHEN** a group currently has four URLs and the user activates save
- **THEN** lists are not persisted and the widget's next check still uses the last valid lists
