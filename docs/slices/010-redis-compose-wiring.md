# 010 — Redis in Compose and Spring wiring (partial)

## Goal
Bring up Redis beside Postgres and connect Spring Boot so a later probe queue has a broker. No enqueue, worker profile, or scheduler change yet.

## Decisions
### Infra before queue code
- Options: Spring Redis client first vs Compose Redis first
- Chose: Compose `redis:7` + healthcheck, then `spring-boot-starter-data-redis` + `localhost:6379`
- Why: same as Postgres — prove the broker is healthy before LPUSH
- Consequences: `@SpringBootTest` may need Redis up or autoconfig excluded; CI Redis deferred. Scheduler still calls `check()` in-process

### Queue / worker
- Deferred: `ProbeQueue` (slug-only list), scheduler enqueue, `@Profile("worker")` + `web-application-type=none`

## Proof
`docker compose ps` → postgres and redis healthy.  
`./gradlew bootRun` → Started; Redis autoconfig present; JPA still owns `CheckResultRepository`.

## Code state
- Ref: working tree vs `70c71ec` (pre-commit)
- Files: `compose.yaml`, `build.gradle`, `application.properties` (main)
