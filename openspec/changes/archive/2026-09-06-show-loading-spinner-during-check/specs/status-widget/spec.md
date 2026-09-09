## MODIFIED Requirements

### Requirement: In-progress and last-check time

While a check is running, the widget MUST show an indeterminate loading spinner and MUST keep that in-progress presentation until the check finishes, including while it determines whether the device has a validated network. The widget MUST NOT replace that in-progress presentation with the previous completed result while the check is still running. After a check finishes (including no-network), the widget MUST hide the spinner and MUST show when that result was produced.

#### Scenario: Tap starts a visible check

- **WHEN** the user taps the widget and a check begins
- **THEN** the widget shows a loading spinner and an in-progress status before the new verdicts or no-network status appear

#### Scenario: Spinner stays through network-availability determination

- **WHEN** a check is running and the system is determining whether the device has a validated network
- **THEN** the loading spinner remains visible until that determination has produced a terminal result (no-network or group verdicts)

#### Scenario: Previous result is not restored during a check

- **WHEN** a check is still running and the widget is redrawn
- **THEN** the widget still shows the loading spinner and does not show the previous completed verdicts or no-network status as if the check had already finished

#### Scenario: Completed check shows its time

- **WHEN** a check completes successfully or with no-network
- **THEN** the widget hides the loading spinner and displays the time of that result
