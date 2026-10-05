File:
  Suggested: docs/slices/008-persist-last-check-postgres.md

Slice note:
---
# 008 — Persist last check in Postgres

## Goal
Last check survives process restart. Catalog still in Java; Flyway owns schema; `ddl-auto=validate`. Tests stay on H2 (no Compose in CI). No history table, no status-page “Check now,” no Redis.

## Decisions
### Postgres how
- Options: local install vs Docker Compose
- Chose: Compose `postgres:16`, user/db `uptime`, port 5432, volume, `pg_isready` healthcheck
- Why: healthcheck before Flyway; laptop-only stack without installing Postgres
- Consequences: host 5432 must be free; `down -v` wipes data

### Schema ownership
- Options: Hibernate `update`/`create` vs Flyway + `validate`
- Chose: Flyway `V1__check_result.sql`; main `ddl-auto=validate`
- Why: weather-insights lesson — migrations are source of truth
- Consequences: entity must match SQL or boot fails; empty Flyway start then V1 apply is fine

### Last check vs history
- Options: append-only history vs one row per slug
- Chose: `slug` PK; each probe overwrites that row
- Why: this slice is “last check across restarts,” not a time series
- Consequences: `SELECT * FROM check_result` shows one row per target; history is a later table/query

### Domain vs persistence
- Options: persist `HttpResponse` / use entity as API type vs map
- Chose: keep `CheckResult` record; `CheckResultEntity` + repository; `toEntity`/`toDomain`
- Why: HTTP response ≠ domain ≠ table row
- Consequences: `@GeneratedValue` on slug is wrong (assigned catalog key)

### Tests vs laptop DB
- Options: CI Postgres service + tests on Compose vs H2 for tests
- Chose: test `application.properties` replaces main file → H2 mem, Flyway off, `create-drop`; CI stays Gradle-only; also set `uptime.probe-interval-ms` and disable scheduling in tests
- Why: same-named test properties replace main (placeholders broke `contextLoads`); `create-drop` on Compose would fight Flyway
- Consequences: Postgres in CI deferred until Testcontainers / Flyway-on-CI slice

### Check now on `/status`
- Deferred to the next slice so this PR stays “persist last check”

## Proof
`./gradlew test` → BUILD SUCCESSFUL (H2 + mocked repository).  
`bootRun` with Compose healthy → Flyway `Migrating … to version "1 - check result"` → `now at version v1`.  
After probe: `docker compose exec postgres psql -U uptime -d uptime -c "SELECT * FROM check_result"` → one `github` row. Restart still serves last check via GET/`/status`.

## Code state
- Ref: working tree vs `c8eae21`
- Files:
  build.gradle                         |  5 +
  compose.yaml                         | 19 +
  .../persistence/CheckResultEntity.java | 59 +
  .../repository/CheckResultRepository.java | 8 +
  .../service/TargetService.java       | 36 +
  application.properties (main)        |  4 +
  db/migration/V1__check_result.sql    |  7 +
  .../TargetServiceTest.java           | 36 +
  application.properties (test)        |  7 +