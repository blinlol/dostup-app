## Purpose

Keeps this repository’s Gradle and GitHub Actions versions current by opening Dependabot version-update pull requests that a human can review and merge.

## ADDED Requirements

### Requirement: Weekly Gradle version-update pull requests

The repository MUST enable GitHub Dependabot version updates for the Gradle ecosystem at the repository root. Those updates MUST cover declared Gradle library dependencies, Gradle plugins, and the Gradle Wrapper. The update interval MUST be weekly. Each update MUST be proposed as a pull request targeting the default branch.

#### Scenario: A Gradle library has a newer release

- **WHEN** a declared Gradle library, plugin, or Wrapper version has a newer release and Dependabot’s weekly Gradle check runs
- **THEN** Dependabot opens a pull request against the default branch that updates that version

### Requirement: Weekly GitHub Actions version-update pull requests

The repository MUST enable GitHub Dependabot version updates for GitHub Actions used by workflows in this repository. The update interval MUST be weekly. Each update MUST be proposed as a pull request targeting the default branch.

#### Scenario: A workflow action has a newer release

- **WHEN** a GitHub Action pin used by a repository workflow has a newer release and Dependabot’s weekly Actions check runs
- **THEN** Dependabot opens a pull request against the default branch that updates that pin

### Requirement: Version-update pull requests wait for a human merge

Dependabot version-update pull requests MUST NOT be merged automatically. They MUST remain open until a human merges or closes them. They MUST be ordinary pull requests so the existing pull-request unit-test check applies.

#### Scenario: Dependabot does not merge its own pull request

- **WHEN** Dependabot opens a version-update pull request and the pull-request unit-test check completes
- **THEN** the pull request stays unmerged until a human merges or closes it
