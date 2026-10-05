# 004 — HTTP probe and in-memory last check

## Goal
GET a catalog URL with a 5s timeout, map the HTTP outcome to a domain `CheckResult`, and keep the last result per slug in memory. No Postgres, API, or Thymeleaf.

## Decisions
### Timeout and “down”
- Options: split connect/read vs one overall budget; down = status ≥ 400 vs only transport failure
- Chose: 5s overall (`HttpRequest.timeout` + `HttpClient.connectTimeout`); `up = status < 400`; connect/timeout/TLS → `up = false`, `statusCode = null`
- Why: user picks for this slice
- Consequences: 3xx counts as up; later dashboards must treat null status as “no HTTP answer,” not as 0

### Where to persist last check
- Options: in-memory map vs Postgres + JPA now
- Chose: `ConcurrentHashMap` last-by-slug
- Why: no Docker/Flyway yet; singleton service can be hit from many Tomcat threads
- Consequences: last check is lost on process restart; Postgres still deferred

### HTTP client
- Options: inject `HttpClient` as a Spring bean vs build it inside `TargetService`; RestTemplate/WebClient vs JDK `HttpClient`
- Chose: field-built JDK `HttpClient`; `BodyHandlers.discarding()`; map to `CheckResult` (not `HttpResponse`)
- Why: no `HttpClient` bean exists; HTTP response ≠ domain; TLS stays on (default)
- Consequences: `new TargetService()` still works in unit tests

### `check` vs `lastCheck`
- Options: one method that always probes vs probe vs read-last
- Chose: `check(key)` always HTTP + store; `lastCheck(key)` map get only (`null` if never probed)
- Why: status page must not GET the target on every refresh
- Consequences: dashboard/JSON later call `lastCheck`; a worker/scheduler later calls `check`

## Proof
`./gradlew test --tests com.project.uptime_status.service.TargetServiceTest --no-daemon` → BUILD SUCCESSFUL  
(`lastCheck` before check is null; unknown key throws; `check("github")` then `lastCheck` same `CheckResult`. Happy path hits live `https://github.com`.)

## Code state
- Ref: working tree vs `6e3295a`
- Files:
  .../service/TargetService.java     | 49 +
  .../service/TargetServiceTest.java | 33 +
