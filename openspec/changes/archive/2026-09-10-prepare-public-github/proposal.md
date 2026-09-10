## Why

The repository is not usable as a public GitHub page for someone with a phone: there is no CI, no tagged APK, and the README only covers Android Studio. A visitor should download a signed APK from the latest release tag, install it, and add the widget, with tests gating every push.

## What Changes

- Make the Gradle build portable (no machine-specific JDK path) and accept `versionName` / `versionCode` from CI on a release tag.
- Sign release APKs with one long-lived keystore supplied via environment / GitHub Secrets; never commit the keystore or passwords.
- Add GitHub Actions: unit tests (and a debug assemble) on push/PR; on tags `vMAJOR.MINOR.PATCH`, run tests, build a signed release APK, and attach it to a GitHub Release as `Dostup.apk`.
- Replace the Studio-only README with a Russian phone-user page: what the widget does, a screenshot, download of the latest APK, sideload steps, how to add the widget, Android 8+, MIT.
- LICENSE is already in the repository; this change does not add or edit it.

Out of scope: contributor docs, issue/PR templates, Play Store / F-Droid, generating a new keystore in CI, and any widget or probe behavior.

## Capabilities

### New Capabilities

- `public-github`: Public GitHub presence for phone users — CI tests, version-from-tag signed APK on GitHub Releases, and a Russian README whose primary path is download-and-install.

### Modified Capabilities

- None. `status-widget`, `access-probe`, and `probe-lists` are unchanged.

## Impact

`gradle.properties`, `app/build.gradle.kts`, new `.github/workflows`, root `README.md`, and a screenshot asset. GitHub Actions secrets for the release keystore (operator-provided, not files in git). No Kotlin runtime, list, or widget UI changes. The existing complete change `add-studio-readme` is superseded for README audience; it is not archived here.
