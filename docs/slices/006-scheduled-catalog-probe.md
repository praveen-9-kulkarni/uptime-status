File:
  Suggested: docs/slices/006-scheduled-catalog-probe.md

Slice note:
---
# 006 — Scheduled catalog probe

## Goal
Last-check JSON and `/status` fill in without a browser refresh triggering GitHub GET. A timer walks the catalog and calls `check(key)`. No Postgres, extra targets, or “check now” POST.

## Decisions
### Where the timer lives
- Options: `@Scheduled` on `TargetService` / a GET controller vs a dedicated component
- Chose: `TargetProbeScheduler` in `schedule`, constructor-injected `TargetService`; `@EnableScheduling` on `UptimeStatusApplication`
- Why: scheduling is when to probe, not how; GET handlers stay on `lastCheck`
- Consequences: same JVM as Tomcat for now; `@Profile("worker")` + `web-application-type=none` still later

### Interval
- Options: `fixedRate` vs `fixedDelay`; hardcoded vs property
- Chose: `@Scheduled(fixedDelayString = "${uptime.probe-interval-ms}")` with `60000`
- Why: a slow probe must not overlap the next tick
- Consequences: local proof can lower the property; restart still wipes in-memory last check

### Batch check
- Options: loop in the scheduler vs `TargetService.checkAll()`
- Chose: `checkAll()` over catalog keys, per-key try/catch, log and continue
- Why: one surprise must not skip the rest of the catalog; scheduler stays one line
- Consequences: catch is `UnknownTargetException` (catalog keys never miss today); `RuntimeException` would be the stronger catch. Unused import `RequestEntity.UriTemplateRequestEntity` should be dropped before ship

## Proof
`./gradlew test` after correcting `checkAll_storesLastCheckForCatalogTargets` (unknown slugs throw; do not `lastCheck("nope")`).  
`curl http://localhost:8080/targets/github` → `{"up":true,"statusCode":200,...}` at `13:17:50Z` then `13:18:50Z` (~60s, different `latencyMs`).  
Browser `/status` → GitHub `true` / `200` / latency / observed (UTC `Instant`). GET did not probe; the scheduler did.

## Code state
- Ref: working tree vs `5fce5d6`
- Files:
  .../UptimeStatusApplication.java |  2 +
  .../service/TargetService.java   | 16 +
  application.properties           |  1 +
  .../TargetServiceTest.java       |  7 +
  .../schedule/TargetProbeScheduler.java (untracked)