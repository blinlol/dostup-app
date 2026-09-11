## Context

See `proposal.md` for motivation. Observed today:

- No `.github/dependabot.yml` (or other Dependabot config).
- Gradle versions are string literals in `build.gradle.kts` and `app/build.gradle.kts` (AGP 8.13.2, Kotlin 2.0.21, AndroidX / Work / Coroutines / JUnit). There is no version catalog or lockfile.
- Wrapper is Gradle 8.13 via `gradle/wrapper/gradle-wrapper.properties`.
- GitHub Actions pins live in `.github/workflows/ci.yml` and `release.yml` (`actions/checkout@v4`, `actions/setup-java@v4`, `android-actions/setup-android@v3`, `softprops/action-gh-release@v2`).
- `public-github` already runs unit tests on pull requests to the default branch (`main`).
- `prepare-public-github` listed Dependabot as a non-goal; this change adds only that missing GitHub config.

## Goals / Non-Goals

**Goals:**

- One committed Dependabot version-updates config that covers the two ecosystems this repo uses.
- Weekly PRs against `main`, reviewed and merged by a human, tested by existing CI.

**Non-Goals:**

- Grouping, ignore rules, reviewers/assignees, custom `open-pull-requests-limit`, or auto-merge.
- Migrating Gradle to a version catalog or lockfile so Dependabot can resolve more graphs.
- Enabling `insecure-external-code-execution` for Gradle.
- Changing Dependabot security-advisory alerts or grouped security PRs.
- Editing workflows, README, or application code.

## Decisions

### 1. Two `updates` entries: `gradle` and `github-actions`, both at `/`

**Choice:** `.github/dependabot.yml` version 2 with:

- `package-ecosystem: gradle`, `directory: "/"` — libraries and plugins in the Kotlin DSL build files, plus the Gradle Wrapper (`gradle-wrapper.properties`).
- `package-ecosystem: github-actions`, `directory: "/"` — Actions used under `.github/workflows/`.

No other ecosystems exist in this repo (no npm, Docker, etc.).

**Why:** Matches the files that actually pin versions. GitHub’s Gradle ecosystem is the documented way to version-update both project deps and the wrapper.

**Alternatives:** Actions-only (leaves AGP/Kotlin/AndroidX stale); a third ecosystem for the wrapper (not a separate Dependabot ecosystem — wrapper is part of `gradle`).

### 2. Weekly interval, GitHub defaults otherwise

**Choice:** `schedule.interval: weekly` on both entries. Leave `open-pull-requests-limit` at GitHub’s default (5). No `groups`, `ignore`, `reviewers`, or `assignees`.

**Why:** The dependency set is small; weekly is GitHub’s recommended cadence and avoids daily noise. Ungrouped PRs keep a failed CI run attributable to one bump. Default PR limit is enough for this graph.

**Alternatives:** Daily (more PRs than this app needs); groups by prefix such as `androidx.*` (fewer PRs, harder to bisect a red CI).

### 3. Do not execute Gradle during resolution

**Choice:** Do not set `insecure-external-code-execution: allow`. Versions are plain string literals in the DSL, which Dependabot can parse without running the build.

**Why:** Allowing script execution is a supply-chain widening we do not need for the current files.

**Alternatives:** Allow it later if a future catalog/scripted version cannot be parsed; that would be a separate change.

### 4. Human merge only; reuse existing CI

**Choice:** No Dependabot auto-merge (workflow or repo setting). Dependabot PRs are normal PRs to `main`, so `.github/workflows/ci.yml` already runs `./gradlew test assembleDebug`.

**Why:** AGP, Kotlin, and AndroidX bumps can break the widget; a human should see CI before merge. Matches `public-github` without editing that spec.

**Alternatives:** Auto-merge patch/minor if CI is green (faster, riskier for Android Gradle bumps).

## Risks / Trade-offs

- **[Dependabot cannot parse a future scripted version]** → a bump is skipped. Mitigation: keep versions as literals; revisit catalog + code execution only if needed.
- **[Wrapper or AGP bump fails CI]** → PR stays open. Mitigation: human merge only; existing unit-test check fails the PR.
- **[Default PR limit of 5 hides later bumps]** → older PRs must be merged or closed first. Mitigation: acceptable for this small graph; raise the limit in a later change if it becomes a problem.
- **[GitHub org/repo setting disables version updates]** → config file is ignored. Mitigation: public repo default is on once `dependabot.yml` exists; operator confirms in repo Settings if PRs never appear.

## Migration Plan

1. Land `.github/dependabot.yml` on the default branch.
2. Wait for GitHub to pick up the config (usually within hours; first weekly job may not fire immediately).
3. Review and merge Dependabot PRs as they appear; CI on the PR is the gate.
4. Rollback: delete or revert `.github/dependabot.yml`; GitHub stops opening new version-update PRs. Open Dependabot PRs can be closed.

## Open Questions

None. Schedule, ecosystems, and no-auto-merge are recorded above as assumptions.
