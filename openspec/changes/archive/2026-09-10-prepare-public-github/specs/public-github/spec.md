## Purpose

Lets a phone user get a tested, signed APK from the public GitHub repository and understand how to install the widget, without contributing code.

## ADDED Requirements

### Requirement: Push and pull request run unit tests

When the default branch is pushed or a pull request targets it, CI MUST run the project's unit tests. The check MUST fail if any unit test fails. CI MUST NOT publish a GitHub Release on an ordinary push or pull request.

#### Scenario: Tests pass on a push

- **WHEN** a push to the default branch contains code that passes all unit tests
- **THEN** the CI check succeeds and no GitHub Release is created for that push

#### Scenario: Tests fail the check

- **WHEN** a push or pull request contains a failing unit test
- **THEN** the CI check fails

### Requirement: Release tag publishes a signed APK

When a git tag matching `vMAJOR.MINOR.PATCH` (non-negative integers) is pushed, CI MUST run the unit tests, then publish a GitHub Release for that tag whose assets include a release-signed APK named `Dostup.apk`. That APK's `versionName` MUST be `MAJOR.MINOR.PATCH` and its `versionCode` MUST equal `MAJOR * 10000 + MINOR * 100 + PATCH`. CI MUST NOT publish a Release if the tests fail or if the APK is not release-signed.

#### Scenario: Tag v1.0.0 publishes Dostup.apk

- **WHEN** the tag `v1.0.0` is pushed and unit tests pass and the release keystore is available to CI
- **THEN** a GitHub Release for `v1.0.0` exists with asset `Dostup.apk` whose `versionName` is `1.0.0` and `versionCode` is `10000`

#### Scenario: Failed tests block the release

- **WHEN** a matching version tag is pushed and a unit test fails
- **THEN** CI does not create a GitHub Release for that tag

### Requirement: Latest APK has a stable download URL

The newest non-prerelease GitHub Release MUST keep the APK asset name `Dostup.apk` so a visitor can download it from the repository's latest-release download URL without knowing the tag name.

#### Scenario: README link does not encode a tag

- **WHEN** a visitor follows the README download link after at least one successful version-tag release
- **THEN** they receive the `Dostup.apk` from the latest non-prerelease Release

### Requirement: One signing key for updates

Every published `Dostup.apk` MUST be signed with the same release keystore so a later tag installs over an earlier published APK. The keystore file and its passwords MUST NOT be stored in the git repository.

#### Scenario: Second tag updates the first install

- **WHEN** a device has `Dostup.apk` from tag `v1.0.0` installed and the user installs `Dostup.apk` from a later matching version tag signed with the same keystore and a higher `versionCode`
- **THEN** the system updates the existing app instead of treating it as a different app

#### Scenario: Keystore is absent from git

- **WHEN** someone clones the repository
- **THEN** the working tree does not contain the release keystore file or its passwords

### Requirement: Phone-user README

The root README MUST be in Russian. It MUST state what the widget does, include a screenshot of the widget on a home screen, link to the latest `Dostup.apk`, and tell the reader how to sideload the APK and add the **Статус доступа** widget on Android 8.0 or newer. The primary path MUST be download-and-install, not Android Studio or contributing.

#### Scenario: Visitor can install from the README

- **WHEN** a reader opens the root README with no development tools
- **THEN** they can see what the widget is, a screenshot, a download link for the latest APK, sideload steps, and how to add the widget

### Requirement: Committed Gradle config is portable

The committed Gradle configuration MUST NOT pin a host-specific JDK filesystem path. A clone on another machine MUST be able to run unit tests using JDK 17 from the environment.

#### Scenario: Clone does not require the original JDK path

- **WHEN** the project is cloned on a machine whose JDK 17 is not at `/usr/lib/jvm/java-17-openjdk-amd64`
- **THEN** `./gradlew test` can run without editing `gradle.properties` to change a JDK path
