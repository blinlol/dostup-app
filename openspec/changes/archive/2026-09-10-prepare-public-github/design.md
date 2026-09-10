## Context

See `proposal.md` for motivation. Observed today:

- No `.github/` workflows, no git tags, no remote.
- `gradle.properties` sets `org.gradle.java.home=/usr/lib/jvm/java-17-openjdk-amd64`, which breaks CI and any other JDK layout.
- `app/build.gradle.kts` hardcodes `versionCode = 1` / `versionName = "1.0"` and has no `signingConfigs`.
- Unit tests already exist and run via `./gradlew test` (JUnit on JVM; no instrumented tests).
- `LICENSE` is MIT and stays as-is.
- Root `README.md` is Russian but Studio-emulator-only (`add-studio-readme`).

`gradlew` is already committed (Gradle 8.13, AGP 8.13.2, JDK 17). Release `isMinifyEnabled` is already false.

## Goals / Non-Goals

**Goals:**

- One portable Gradle module that CI can test and, on a version tag, sign and version from the tag.
- Two GitHub Actions entry points: verify on push/PR; publish `Dostup.apk` on `vMAJOR.MINOR.PATCH`.
- README rewritten for sideload; screenshot committed as a repo asset.

**Non-Goals:**

- Changing widget/probe code or adding instrumented CI.
- Contributor files, Dependabot, Play/F-Droid.
- Generating or rotating the keystore inside CI.
- Rewriting git history or creating the GitHub remote (operator).

## Decisions

### 1. Version comes from the tag, with a stable versionCode formula

**Choice:** Tags MUST match `vMAJOR.MINOR.PATCH`. CI passes Gradle properties:

- `appVersionName` = `MAJOR.MINOR.PATCH` (no `v`)
- `appVersionCode` = `MAJOR * 10000 + MINOR * 100 + PATCH`

`app/build.gradle.kts` reads those properties when present; otherwise keeps a local fallback (`versionName` `0.0.0-dev`, `versionCode` `1`) so an untagged `assemble` still works. MINOR and PATCH are 0–99; that is enough for this app and keeps `versionCode` monotonic as long as tags never go backwards.

**Why:** Matches the agreed “CI derives version” rule and avoids a forgotten `versionCode` bump that would block sideload updates.

**Alternatives:** Hand-edit Gradle before each tag (easy to skip); `github.run_number` as `versionCode` (not derived from the tag; rebuilds and tag math diverge).

### 2. Signing only when a keystore is supplied; one keystore forever

**Choice:** `signingConfigs.release` is applied to `buildTypes.release` only if store file + passwords + alias are present (Gradle properties or env). CI on a tag writes a temporary `.jks` from secret `KEYSTORE_BASE64` and sets `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`. Nothing of that is committed. Debug builds stay debug-signed.

The operator creates the keystore once with `keytool`, keeps an offline copy, and stores the four secrets on the GitHub repo. CI never runs `keytool -genkeypair`.

**Why:** Same key across tags is what makes `package:ru.wlwidget` updatable. Generating a key in CI would mint a new key every job.

**Alternatives:** Always debug-sign published APKs (updates break if the debug keystore changes); commit a keystore (leaks the update key).

### 3. Two workflows, APK name stable

**Choice:**

- `.github/workflows/ci.yml` — `push`/`pull_request` to the default branch: JDK 17, `./gradlew test assembleDebug`.
- `.github/workflows/release.yml` — `push` tags `v*.*.*`: parse the tag, `./gradlew test assembleRelease` with version properties + signing env, upload `Dostup.apk` (copy/rename from `app/build/outputs/apk/release/`) onto a GitHub Release for that tag via `softprops/action-gh-release` (or equivalent). Non-matching tags do nothing useful; invalid parse fails the job.

**Why:** Test CI must not need secrets. Release CI must. A fixed asset name keeps `/releases/latest/download/Dostup.apk` stable.

**Alternatives:** One workflow with `if: tags` (secrets still have to be guarded); publish `app-release.apk` (ugly URL, easy to break if AGP changes the filename).

### 4. Drop the pinned JDK path

**Choice:** Remove `org.gradle.java.home` from committed `gradle.properties`. Actions uses `actions/setup-java` (Temurin 17). Local and CI use `JAVA_HOME` / the Gradle toolchain on PATH.

**Why:** The current path is this machine only; CI would fail before any test ran.

### 5. README is a sideload page; screenshot is a committed image

**Choice:** Replace the Studio-only README with Russian sections: one-line what, screenshot, download latest `Dostup.apk`, unknown-sources + install, add **Статус доступа**, Android 8.0+, MIT / no account. Studio/CLI at most a short collapsed footnote, not the lead. Download URL is `https://github.com/<owner>/<repo>/releases/latest/download/Dostup.apk` taken from `origin` when it exists; otherwise a visible placeholder the operator fills after creating the GitHub repo.

Screenshot path: `docs/widget.png` (home screen with the widget). Capture is an operator/apply step (emulator or device); the README MUST NOT ship without that file.

**Why:** The public audience is a phone user. A README without a picture does not explain a widget.

**Alternatives:** Keep the Studio README as primary (wrong audience); link only to the Releases page without a direct APK URL (extra click, less obvious).

## Risks / Trade-offs

- **[Lost keystore]** → later APKs cannot update existing installs. Mitigation: operator backup of `.jks` + passwords outside git; documented as a required human step before the first tag, not as a CI job.
- **[First install vs debug build]** → a debug-signed app already on a phone will not update to `Dostup.apk`. Mitigation: README notes uninstall once before the first GitHub APK.
- **[Release job runs before secrets exist]** → tag workflow fails. Mitigation: tasks put keystore + GitHub Secrets before the first tag; CI-on-push still lands without secrets.
- **[MINOR or PATCH ≥ 100]** → `versionCode` formula collides. Mitigation: spec/formula assume 0–99; this app will not need that range.
- **[No GitHub remote at apply time]** → README download URL cannot be finalized. Mitigation: placeholder + operator fill-in when `origin` exists.
- **[No screenshot yet]** → README requirement unmet. Mitigation: explicit capture task; do not treat README as done without `docs/widget.png`.

## Migration Plan

1. Land portable Gradle + signing-from-env + workflows on the default branch (unsigned/debug CI only until secrets exist).
2. Operator: `keytool`, backup, GitHub Secrets; create the public repo and `origin` if missing.
3. First public tag `v1.0.0` (`versionCode` 10000). Anyone with a debug build uninstalls once, then installs `Dostup.apk`.
4. Later tags only need a bumped `vX.Y.Z`; no Gradle version edit.

Rollback: delete a bad GitHub Release and tag; do not reuse a tag with a lower/equal `versionCode`. Lost signing key has no rollback except a new applicationId (out of scope).

## Open Questions

None that affect specs or the task breakdown. GitHub owner/repo is an apply-time fill-in from `origin`.
