## ADDED Requirements

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
