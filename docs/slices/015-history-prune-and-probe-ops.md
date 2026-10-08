# 015 — History prune and probe ops

## Goal

Cap `check_result_history` growth per target, make probe cadence and retention tunable via config, improve operator visibility into scheduled and queued probes, and allow HTTPS probes from Docker on a TLS-inspected corporate network.

Done when: each slug retains only the newest *K* history rows (configurable), prune runs on a schedule (interval + keep count in properties), probe worker/scheduler emit info logs around work, default probe interval is 60s for local dev, and the app image trusts the Flipkart root CA for outbound HTTPS.

## Decisions

### How to trim history per target

- **Options:** Time-based TTL delete; keep newest *K* rows per `slug`; global row cap.
- **Chose:** Keep newest *K* per `slug` via `CheckResultHistoryRepository.deleteAllButFirstKPerSlug` (native `DELETE … USING (SELECT id … ORDER BY observed_at DESC, id DESC OFFSET :k)`).
- **Why:** Matches per-target history UI; predictable row count per slug; `OFFSET`/`ORDER BY` matches “newest first” semantics.
- **Consequences:** Postgres-specific native query; `@Modifying(clearAutomatically = true)`; `TargetService.pruneHistory` validates catalog key and `k > 0` before delegating.

### Retention and probe tuning in config

- **Options:** Hard-coded intervals; env-only overrides; `application.properties` keys.
- **Chose:** `uptime.history-prune-interval-ms`, `uptime.history-keep-per-slug`, and `uptime.probe-interval-ms=60000` (was 300000 in main `application.properties`); mirror prune keys in test properties with scheduling disabled.
- **Why:** Same pattern as existing `uptime.probe-interval-ms`; faster local feedback without code changes.
- **Consequences:** Operators must set keep count and prune interval per environment; probe load increases with 60s interval.

### Probe observability

- **Options:** Debug-only logs; metrics; info logs at enqueue/dequeue boundaries.
- **Chose:** `log.info` on `TargetProbeScheduler.probeAllTargets` (“Probing all targets”) and on `ProbeWorker` before/after `targetService.check` for the dequeued key.
- **Why:** Low-cost trace that probes are scheduling and draining without changing queue semantics.
- **Consequences:** Log volume scales with target count and worker throughput; no structured fields beyond target key.

### Corporate TLS in Docker

- **Options:** `InsecureSkipVerify` in app code; custom `TrustManager`; import CA into JRE `cacerts` at image build.
- **Chose:** `COPY certs/FlipkartRootCA.cer` and `keytool -importcert` into `$JAVA_HOME/lib/security/cacerts` in the Dockerfile, then remove the cert file from the image layer.
- **Why:** JVM default trust store handles HTTPS clients without app changes; required for outbound HTTPS through corp inspection.
- **Consequences:** Image is network-specific; `storepass changeit` is the default cacerts password; `certs/` is an untracked build input in this working tree.

## Proof

not recorded

## Code state

- **Ref:** `1aad485` on `main` (uncommitted working tree).
- **Files (`git diff --stat`):** `Dockerfile`; `ProbeWorker.java`; `CheckResultHistoryRepository.java`; `TargetProbeScheduler.java`; `TargetService.java`; `application.properties`; `TargetServiceTest.java`; `src/test/resources/application.properties` — 8 files, +79 / −3.
- **Also present (untracked, not in stat):** `certs/`; `HistoryPruneScheduler.java`; `src/test/java/com/project/uptime_status/schedule/`.
