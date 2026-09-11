## 1. Dependabot config

- [x] 1.1 Add `.github/dependabot.yml` version 2 with `package-ecosystem: gradle` and `package-ecosystem: github-actions`, both `directory: "/"`, both `schedule.interval: weekly`, and without `groups`, `ignore`, `insecure-external-code-execution`, reviewers, or auto-merge keys; and verify the file contains those two ecosystems, `/`, and `weekly`, and does not contain `insecure-external-code-execution` or auto-merge
- [x] 1.2 Leave existing workflows unchanged so Dependabot PRs stay ordinary pull requests: confirm `.github/workflows/ci.yml` still triggers on `pull_request` to the default branch and that no workflow file auto-merges Dependabot PRs
