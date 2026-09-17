## MODIFIED Requirements

### Requirement: Odd size of at least five

Each group MUST contain an odd number of addresses and MUST contain at least five addresses.

#### Scenario: Bundled lists meet the size rule

- **WHEN** the app uses the bundled lists
- **THEN** each of the three groups has an odd address count of five or more

#### Scenario: Saved lists meet the size rule

- **WHEN** the user successfully saves lists
- **THEN** each of the three groups has an odd address count of five or more

#### Scenario: Invalid size is not saved

- **WHEN** the user tries to save lists and any group has an even count or fewer than five addresses
- **THEN** the save is refused and later checks keep using the last valid lists (saved overlay or bundled defaults)

### Requirement: Bundled versioned lists

The app MUST ship the three groups as a versioned data file. Checks MUST NOT require a network download of the lists in order to run. Until the user saves lists, and again after a reset, checks MUST use the bundled lists currently shipped in the app, including after an app update.

#### Scenario: First launch with no extra download

- **WHEN** the app runs a check on a device that has never fetched remote lists
- **THEN** probes use the bundled lists

#### Scenario: Never-saved device uses bundled lists

- **WHEN** a check runs and the user has never successfully saved lists
- **THEN** probes use the bundled lists currently shipped in the app

#### Scenario: App update replaces unsaved defaults

- **WHEN** the app is updated with new bundled lists and the user has never successfully saved lists
- **THEN** subsequent checks use the new bundled lists

## ADDED Requirements

### Requirement: User overlay replaces all three groups

The user MUST be able to replace the addresses used for whitelist, ordinary, and blocked. A successful save MUST persist all three groups together. After a successful save, checks MUST use those saved groups until the user resets. An in-progress edit that has not been saved MUST NOT change the addresses used by checks.

#### Scenario: Saved lists are used by the next check

- **WHEN** the user successfully saves valid lists and a check runs
- **THEN** that check probes the saved whitelist, ordinary, and blocked addresses rather than the bundled lists

#### Scenario: Unsaved edits are ignored by checks

- **WHEN** the user has changed URLs on screen but has not successfully saved and a check runs
- **THEN** that check still uses the last valid lists (saved overlay or bundled defaults)

#### Scenario: Saved overlay survives an app update

- **WHEN** the user has successfully saved lists and the app is later updated with different bundled lists
- **THEN** subsequent checks keep using the saved lists until the user resets

### Requirement: Reset restores current bundled lists

The user MUST be able to discard saved lists. After reset, checks MUST use the bundled lists currently shipped in the app.

#### Scenario: Reset returns to bundled lists

- **WHEN** the user has saved lists and then resets
- **THEN** subsequent checks use the bundled lists currently shipped in the app

### Requirement: Saved addresses are HTTPS URLs

Every saved address MUST be an `https://` URL with a host. Leading and trailing whitespace MUST be ignored. A group MUST NOT contain the same URL more than once. A save that violates these rules MUST be refused, and checks MUST keep using the last valid lists.

#### Scenario: HTTP or empty host is refused

- **WHEN** the user tries to save a group that includes an `http://` URL or a URL with no host
- **THEN** the save is refused and later checks keep using the last valid lists

#### Scenario: Duplicate in a group is refused

- **WHEN** the user tries to save a group that contains the same URL twice after whitespace is trimmed
- **THEN** the save is refused and later checks keep using the last valid lists

### Requirement: Unreadable overlay falls back to bundled lists

If saved lists cannot be read as three valid groups, checks MUST use the bundled lists and MUST still run.

#### Scenario: Corrupt overlay does not block checks

- **WHEN** a check runs and the saved overlay is missing required groups, fails the size rule, or cannot be parsed
- **THEN** probes use the bundled lists
