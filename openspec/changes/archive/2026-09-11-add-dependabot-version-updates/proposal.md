## Why

Gradle libraries, plugins, the Gradle Wrapper, and GitHub Actions pins in this repo age in place: there is no Dependabot config, and `prepare-public-github` left that out on purpose. Version updates should open ordinary pull requests so CI already required by `public-github` can gate them before a human merges.

## What Changes

- Add a committed GitHub Dependabot version-updates config (`.github/dependabot.yml`) for the ecosystems this repo actually uses: Gradle (including the wrapper) and GitHub Actions.
- Keep the existing CI and release workflows as the test/publish path; Dependabot PRs MUST go through the same unit-test check as any other PR.
- Do not change app code, version catalogs, auto-merge, grouping, or Dependabot security-advisory settings (GitHub’s default security updates stay as they are).

## Capabilities

### New Capabilities

- `dependabot`: GitHub Dependabot version updates for this repository’s Gradle and GitHub Actions dependencies, on a weekly schedule, as pull requests against the default branch.

### Modified Capabilities

- None. `public-github` already requires unit tests on pull requests targeting the default branch; Dependabot PRs reuse that. `status-widget`, `access-probe`, and `probe-lists` are unchanged.

## Impact

New `.github/dependabot.yml` only. No Kotlin, Gradle build logic, README, or workflow file changes. After merge, GitHub will open version-update PRs; those PRs run `.github/workflows/ci.yml`. Operator must have Dependabot version updates enabled on the GitHub repo (default for public repos once the config file exists).
