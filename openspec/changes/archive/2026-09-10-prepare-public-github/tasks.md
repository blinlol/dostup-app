## 1. Portable Gradle

- [x] 1.1 Remove `org.gradle.java.home` from committed `gradle.properties` and verify that file no longer pins a JDK filesystem path
- [x] 1.2 Read `appVersionName` / `appVersionCode` in `app/build.gradle.kts` with fallback `versionName` `0.0.0-dev` and `versionCode` `1`, and verify `./gradlew test` still passes and the project syncs with `-PappVersionName=1.0.0 -PappVersionCode=10000`

## 2. Keystore

- [x] 2.1 Apply `signingConfigs.release` to the release build type only when store file, passwords, and alias are provided (properties or env), gitignore `*.jks` and `*.keystore`, and verify `./gradlew assembleRelease` without those values still completes and no keystore file is tracked by git
- [x] 2.2 Operator: create one release keystore with `keytool`, keep an offline backup, and set GitHub secrets `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, and `KEY_PASSWORD`, and verify those four secret names exist on the repo and the `.jks` is not in git

## 3. Tests (CI)

- [x] 3.1 Add `.github/workflows/ci.yml` for `push` and `pull_request` to the default branch: Temurin JDK 17, `./gradlew test assembleDebug`, no GitHub Release, and verify the workflow file has those triggers, does not publish a release, and `./gradlew test assembleDebug` passes locally

## 4. APK (release CI)

- [x] 4.1 Add `.github/workflows/release.yml` on tags `v*.*.*` that parses `MAJOR.MINOR.PATCH`, runs unit tests, builds a signed `assembleRelease` with the four keystore secrets and `-PappVersionName` / `-PappVersionCode` (`versionCode = MAJOR * 10000 + MINOR * 100 + PATCH`), and uploads asset `Dostup.apk` to a GitHub Release for that tag, and verify the workflow file encodes that formula, the `Dostup.apk` asset name, and that it does not run `keytool -genkeypair`

## 5. README

- [x] 5.1 Add `docs/widget.png` of the widget on a home screen and verify the file exists
- [x] 5.2 Replace root `README.md` with a Russian phone-user page: what the widget does, the screenshot, a latest-release download link for `Dostup.apk` (from `origin` or a visible placeholder), sideload steps, add **Статус доступа**, Android 8.0+, uninstall-debug-once note, MIT; and verify Studio/CLI is not the primary path and `LICENSE` is unchanged
