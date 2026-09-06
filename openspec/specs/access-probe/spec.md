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

Starting a check MUST go through one pipeline that accepts a refresh trigger and returns group verdicts or no-network. The system MUST provide a tap trigger. The pipeline MUST accept additional triggers later without changing verdict rules.

#### Scenario: Tap runs the same pipeline

- **WHEN** the user taps the widget
- **THEN** the tap trigger starts the shared check pipeline and the widget receives the resulting verdicts or no-network
