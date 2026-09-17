## MODIFIED Requirements

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

## ADDED Requirements

### Requirement: Save control stays off until lists are valid

The save control MUST NOT persist lists while any group fails the odd-count-of-at-least-five rule or the HTTPS URL rules. The user MUST be able to edit toward a valid set without those in-progress values becoming the widget's probe targets.

#### Scenario: Save does nothing while a group is too small

- **WHEN** a group currently has four URLs and the user activates save
- **THEN** lists are not persisted and the widget's next check still uses the last valid lists
