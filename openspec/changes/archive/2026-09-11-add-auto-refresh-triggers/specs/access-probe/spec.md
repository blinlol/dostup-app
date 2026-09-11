## MODIFIED Requirements

### Requirement: Trigger-agnostic check pipeline

Starting a check MUST go through one pipeline that accepts a refresh trigger and returns group verdicts or no-network. The system MUST provide three triggers: tap, a change of the device's default network path (Wi‑Fi, cellular, VPN, or loss of a validated default network), and a periodic interval of 15 minutes. All three triggers MUST use the same verdict rules and the same no-network skip. Automatic triggers MUST run only while at least one widget instance is placed.

#### Scenario: Tap runs the same pipeline

- **WHEN** the user taps the widget
- **THEN** the tap trigger starts the shared check pipeline and the widget receives the resulting verdicts or no-network

#### Scenario: Default path change runs the same pipeline

- **WHEN** the device's default network path changes and at least one widget instance is placed
- **THEN** the path-change trigger starts the shared check pipeline and the widget receives the resulting verdicts or no-network

#### Scenario: Periodic interval runs the same pipeline

- **WHEN** 15 minutes have elapsed since the last periodic trigger and at least one widget instance is placed
- **THEN** the periodic trigger starts the shared check pipeline and the widget receives the resulting verdicts or no-network

#### Scenario: Automatic triggers stop when no widget remains

- **WHEN** the last widget instance is removed
- **THEN** path-change and periodic triggers do not start checks until a widget is placed again

## ADDED Requirements

### Requirement: Trigger preemption

A tap MUST replace an in-flight automatic check. An automatic check MUST NOT interrupt a tap-started check. A default-path change MAY replace an in-flight automatic check. A periodic trigger MUST NOT start a check while another check is already running and MUST NOT queue an extra check behind it. A burst of default-path changes MUST result in a single automatic check rather than one check per event.

#### Scenario: Tap replaces an automatic check

- **WHEN** an automatic check is running and the user taps the widget
- **THEN** the tap-started check runs and the interrupted automatic check does not complete as the widget's result

#### Scenario: Automatic trigger does not interrupt tap

- **WHEN** a tap-started check is running and a path-change or periodic trigger fires
- **THEN** the tap-started check continues and no additional automatic check starts until it finishes

#### Scenario: Path change replaces an automatic check

- **WHEN** an automatic check is running and the default network path changes
- **THEN** a new automatic check runs for the new path

#### Scenario: Periodic tick is ignored while a check is running

- **WHEN** a check is already running and the 15-minute periodic trigger fires
- **THEN** no additional check is started or queued

#### Scenario: Path-change burst becomes one check

- **WHEN** the default network path changes several times in a short burst
- **THEN** only one automatic check runs for that burst
