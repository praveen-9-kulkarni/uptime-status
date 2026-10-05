# 002 — GitHub Actions CI (tests only)

## Goal
Run `./gradlew test` on GitHub-hosted Ubuntu so a green laptop is not the merge signal. No Postgres/Redis in CI until tests need them.

## Decisions
### How much of weather-insights CI to copy
- Options: copy their full `ci.yml` (Postgres + Redis services) vs a job that only runs Gradle tests
- Chose: checkout + Temurin 21 + Gradle cache + `./gradlew test --no-daemon` on `ubuntu-latest`; `on: push` and `pull_request`; job name `test`
- Why: this app’s tests still have no datastore. Their extra services would be cargo-cult.
- Consequences: later add CI services when tests talk to Postgres/Redis. Workflow YAML does not block merge by itself — requiring the `test` check on `main` is still deferred.

## Proof
Not recorded in this thread (no Actions run URL or log pasted). Intended check: push the workflow, then the Actions tab shows job `test` green.

## Code state
- Ref: working tree vs `d17b584`
- Files:
  .github/workflows/ci.yml (untracked)
