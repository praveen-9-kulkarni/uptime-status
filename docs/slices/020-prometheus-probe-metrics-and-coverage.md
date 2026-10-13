# 020 — Prometheus probe metrics and coverage gate

## Goal

Expose probe queue, worker, and per-target check metrics for Prometheus scraping; run the worker management endpoint on a dedicated port; tighten public actuator access on the main app; add a local JaCoCo line-coverage gate on `./gradlew check`; extend tests so `TargetService` and new suites compile against Micrometer.

## Decisions

### Metrics stack

- **Options:** Log-only ops signals; ad-hoc metrics; Micrometer + Prometheus registry.
- **Chose:** Micrometer (`micrometer-registry-prometheus`) with `uptime.probe.queue.size` gauge, `uptime.worker.errors` counter, `uptime.probe` counter (tags `slug`, `outcome`), and `uptime.probe.latency` timer (tag `slug`).
- **Why:** Fits Spring Boot management/prometheus exposure already in use elsewhere in the project.
- **Consequences:** `MeterRegistry` injected into `ProbeQueue`, `ProbeWorker`, and `TargetService`; queue size gauge swallows Redis errors as `NaN`.

### Worker scrape surface

- **Options:** Keep `spring.main.web-application-type=none` with no HTTP; dedicated management server only.
- **Chose:** `management.server.port=8081` and `management.endpoints.web.exposure.include=health,prometheus` in `application-worker.properties`; compose maps `8081:8081` on the worker service.
- **Why:** Scrape worker metrics without turning the worker into a full web application.
- **Consequences:** Worker process listens on 8081 for management; compose must publish that port for local Prometheus.

### Main app actuator exposure (security)

- **Options:** Continue permitting all `GET /actuator/**`; permit only health and prometheus.
- **Chose:** `GET` permit for `/actuator/health/**` and `/actuator/prometheus` only (status, targets, static assets unchanged).
- **Why:** Prometheus and K8s probes need health/prometheus; other actuator endpoints should not be anonymously reachable.
- **Consequences:** Any other actuator endpoint on the main app requires authentication unless policy is changed again; main app also exposes `prometheus` via `management.endpoints.web.exposure.include`.

### Worker probe failures

- **Options:** Let `targetService.check` exceptions abort the scheduled tick; catch, log, and count.
- **Chose:** `try/catch` in `ProbeWorker` with `errorCounter.increment()` and error log; successful path unchanged.
- **Why:** One bad target or transient failure should not break the worker loop.
- **Consequences:** Failed checks are visible in metrics/logs; behavior depends on whether `check` throws vs. records down (existing service semantics).

### Coverage enforcement

- **Options:** No coverage tooling; JaCoCo report only; report + verification rule.
- **Chose:** JaCoCo plugin, `jacocoTestReport` finalized by `test`, `jacocoTestCoverageVerification` with 80% line minimum, wired into `check` only (not plain `test`).
- **Why:** Local/pre-merge quality gate without changing default CI `test` task behavior noted in Gradle comment.
- **Consequences:** `./gradlew check` fails below 80% lines; `./gradlew test` alone does not run verification.

## Proof

not recorded

## Code state

- **Ref:** `5167a85` on `main` (uncommitted working tree)
- **Files:** `build.gradle` (+27); `compose.yaml` (+2); `SecurityConfig.java` (+1/−1); `ProbeQueue.java` (+18/−1); `ProbeWorker.java` (+18/−3); `TargetService.java` (+10/−1); `application-worker.properties` (+3/−1); `application.properties` (+4/−1); `TargetServiceTest.java` (+22/−2); untracked `WorkerSecurityConfig.java`; untracked tests under `controller/`, `queue/`, and `TargetProbeSchedulerTest.java` (9 tracked files: +96/−10 per `diff --stat`)
