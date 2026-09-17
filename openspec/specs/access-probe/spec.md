# access-probe Specification

## Purpose

Classifies what the phone's current network path can open by probing three address groups and reducing each group to one majority verdict.

## Requirements

### Requirement: Measure the active path as-is

Checks MUST use the device's current default network path (Wi‑Fi, cellular, or VPN as currently applied). Checks MUST NOT switch the device to a different network or force traffic onto cellular while another network is active.

#### Scenario: Check while on Wi-Fi

- **WHEN** the default network is Wi-Fi and the user starts a check
- **THEN** probes are sent on that Wi-Fi path and the device is not moved onto cellular for the check

#### Scenario: Check while a VPN is active

- **WHEN** a VPN is the active path and the user starts a check
- **THEN** probes follow that VPN path

### Requirement: HTTP response is success

A single address probe MUST count as success when an HTTP response is received (any status code). A probe MUST count as failure when it times out or fails during DNS, transport, or TLS before an HTTP response arrives.

#### Scenario: Site returns an HTTP status

- **WHEN** a probe receives an HTTP response with any status code within the time limit
- **THEN** that address counts as success

#### Scenario: Handshake or timeout with no HTTP response

- **WHEN** a probe times out or fails during DNS, transport, or TLS and no HTTP response arrives
- **THEN** that address counts as failure

### Requirement: Majority verdict per group

Each group MUST produce exactly one verdict: available if a majority of its addresses succeed, otherwise not available. Each group MUST contain an odd number of addresses greater than three so a majority exists without a tie.

#### Scenario: Three successes out of five

- **WHEN** a group has five addresses and three of them succeed
- **THEN** the group verdict is available even if the remaining probes have not finished

#### Scenario: Three failures out of five

- **WHEN** a group has five addresses and three of them fail
- **THEN** the group verdict is not available even if the remaining probes have not finished

### Requirement: No validated network skips probes

When the device has no validated network, the system MUST report no-network and MUST NOT send address probes.

#### Scenario: Refresh with no validated network

- **WHEN** a check is requested and the device has no validated network
- **THEN** the result is no-network and no address probes are sent

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

### Requirement: Saving lists starts a user-initiated check when a widget exists

After lists are successfully saved, if at least one widget instance is placed, the system MUST start the shared check pipeline as a user-initiated check: the same in-progress presentation as a tap (spinner and in-progress cells) and the same preemption as a tap (replace an in-flight automatic check; must not be interrupted by an automatic trigger). That check MUST use the newly saved lists. If no widget instance is placed, saving MUST persist lists and MUST NOT send address probes.

#### Scenario: Save with a widget starts a visible check

- **WHEN** the user successfully saves lists and at least one widget instance is placed
- **THEN** a user-initiated check starts, the widget shows the tap in-progress presentation, and the check probes the newly saved lists

#### Scenario: Save without a widget does not probe

- **WHEN** the user successfully saves lists and no widget instance is placed
- **THEN** the lists are persisted and no address probes are sent

#### Scenario: Save replaces an automatic check

- **WHEN** an automatic check is running, at least one widget instance is placed, and the user successfully saves lists
- **THEN** the save-started check runs with the newly saved lists and the interrupted automatic check does not complete as the widget's result
