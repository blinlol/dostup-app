## Why

The repository has no README. Someone who already has Android Studio cannot tell how to run the app, and the launcher screen is easy to mistake for the product. A short root README should get them onto the emulator home screen with the widget in one sitting.

## What Changes

- Add a Russian `README.md` at the repository root for the Android Studio emulator path only.
- Cover: open the project, pick an emulator (API 26+), Run, add the **Статус доступа** widget from the home-screen picker, tap it to start a check.
- State that the launcher icon only explains how to add the widget, and that emulator probes use the host computer's network.

## Capabilities

### New Capabilities

- None. Docs-only; this change sets `skip_specs: true` in `.openspec.yaml`.

### Modified Capabilities

- None. Product requirements in `status-widget`, `access-probe`, and `probe-lists` are unchanged.

## Impact

New `README.md` at the repo root. No application code, Gradle, manifests, or runtime behavior. Out of scope: installing Android Studio from scratch, CLI (`gradlew` / `adb`), and a physical device.
