File:
  Suggested: docs/slices/011-probe-queue-worker.md

Slice note:
---
# 011 — Probe queue and worker profile

## Goal
Scheduled probes enqueue catalog slugs on Redis; a separate `worker` process pops and runs `check()`. Check now / POST stay synchronous. Compose Redis healthcheck tightened.

## Decisions
### Producer vs in-process checkAll
- Options: keep scheduler → `checkAll()` vs LPUSH slug then worker `check()`
- Chose: scheduler enqueues; worker dequeues and checks
- Why: Redis is the buffer between “due” and “probe”; API process need not do HTTP probes on the timer
- Consequences: without a worker, `uptime:probe` grows; Check now / POST `/targets/{key}/check` still call `check()` directly

### Queue payload and ownership
- Options: push full Target / URL vs slug only; validate in queue vs trust callers
- Chose: slug string on list key `uptime:probe`; `ProbeQueue` validates via `TargetService.getTarget` before LPUSH; LPUSH + RPOP (FIFO)
- Why: catalog stays in the service; queue stays HTTP-agnostic; unknown keys never land in Redis
- Consequences: producer and consumer share `ProbeQueue` for the key; no central constants class

### Worker process split
- Options: same JVM as API vs `@Profile("worker")` + `web-application-type=none`; `@Scheduled` poll vs blocking BRPOP
- Chose: `ProbeWorker` under `worker` only; `application-worker.properties` disables web; scheduler `@Profile("!worker")`; `@Scheduled(fixedDelay = 1000)` + non-blocking RPOP
- Why: profile alone does not remove Tomcat; worker must not also enqueue; poll is enough for the lesson
- Consequences: run one consumer (laptop worker for now); Compose worker service deferred; do not run two workers on the same list

### Tests without Redis in CI
- Options: exclude Redis autoconfig in test props vs require Redis up vs leave alone
- Chose: leave alone after `./gradlew clean test` with Compose Redis stopped still passed
- Why: Lettuce is lazy; suite never LPUSH/RPOP; CI Redis still deferred
- Consequences: revisit if `contextLoads` starts failing or a queue integration test is added

## Proof
`docker compose exec redis redis-cli LRANGE uptime:probe 0 -1` → `"github"` after API+scheduler; empty after worker.  
`./gradlew bootRun --args='--spring.profiles.active=worker'` → profile `worker`, no Tomcat.  
`psql` `check_result` → `github` up, status 200, `observed_at` advanced.  
`./gradlew clean test` with Redis container down → BUILD SUCCESSFUL.

## Code state
- Ref: working tree vs `3a4e506`
- Files:
  compose.yaml | 2 +-
  .../schedule/TargetProbeScheduler.java | 9 +++++++--
  (untracked) queue/ProbeQueue.java, queue/ProbeWorker.java, application-worker.properties