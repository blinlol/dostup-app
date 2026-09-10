## Purpose

Supplies the three address groups used for classification from a versioned bundled file, with a source that can later be swapped for a network download.

## ADDED Requirements

### Requirement: Three role-based groups

The list source MUST provide three named groups: whitelist (sites expected to stay up in whitelist-only mode), ordinary (sites expected to stay up under everyday blocking and fail in whitelist-only mode), and blocked (sites typically closed under everyday blocking).

#### Scenario: Consumers receive three groups

- **WHEN** a check asks the list source for targets
- **THEN** it receives the whitelist, ordinary, and blocked groups and no other groups

### Requirement: Odd size of at least five

Each group MUST contain an odd number of addresses and MUST contain at least five addresses.

#### Scenario: Bundled lists meet the size rule

- **WHEN** the app uses the bundled lists
- **THEN** each of the three groups has an odd address count of five or more

### Requirement: Bundled versioned lists

This change MUST ship the three groups inside the app as a versioned data file. Checks MUST NOT require a network download of the lists in order to run.

#### Scenario: First launch with no extra download

- **WHEN** the app runs a check on a device that has never fetched remote lists
- **THEN** probes use the bundled lists

### Requirement: Replaceable list source

Check and widget logic MUST obtain addresses only through the list source. A later change MUST be able to add a network-backed source that returns the same three groups without changing verdict rules or widget fields.

#### Scenario: Same groups after a future source swap

- **WHEN** a later change supplies lists from the network through the same source
- **THEN** checks still request whitelist, ordinary, and blocked groups and still apply the same majority verdict rules
