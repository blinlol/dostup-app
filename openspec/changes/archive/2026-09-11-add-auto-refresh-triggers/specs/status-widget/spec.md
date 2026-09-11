## MODIFIED Requirements

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
