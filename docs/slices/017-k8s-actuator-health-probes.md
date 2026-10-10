# 017 — Kubernetes-style actuator health probes

## Goal

Make liveness and readiness behavior explicit in `application.properties` so orchestrators can use `/actuator/health/liveness` and `/actuator/health/readiness` without tying pod restarts to Postgres or Redis outages.

## Decisions

### Enable probe endpoints

- **Options:** Rely on Spring Boot defaults vs set `management.endpoint.health.probes.enabled=true`.
- **Chose:** Enable explicitly.
- **Why:** Comments in config note probes are often on already; explicit setting documents intent for ops and reviewers.
- **Consequences:** Liveness/readiness sub-endpoints are clearly part of the contract.

### Liveness group

- **Options:** Include dependency health (db, redis) in liveness vs only process lifecycle.
- **Chose:** `management.endpoint.health.group.liveness.include=livenessState` only.
- **Why:** Avoid restarting the JVM when dependencies are temporarily unavailable.
- **Consequences:** A wedged app may still be “live”; dependency failure surfaces on readiness instead.

### Readiness group

- **Options:** Minimal readiness vs include data stores.
- **Chose:** `readinessState,db,redis`.
- **Why:** Traffic should not hit an instance that cannot reach Postgres or Redis.
- **Consequences:** Readiness fails when DB or Redis is down; load balancers should stop sending traffic.

### Health details visibility

- **Options:** `when-authorized` / `never` vs `always`.
- **Chose:** `management.endpoint.health.show-details=always` for local dev.
- **Why:** Easier to verify probe groups without auth; comment notes tightening later if auth is added.
- **Consequences:** Full health JSON may leak dependency status on exposed actuator endpoints until tightened.

## Proof

not recorded

## Code state

- **Ref:** `2b72427` (main) plus uncommitted local edits
- **Files:** `src/main/resources/application.properties` (+13 / −1): probe enablement, liveness/readiness group includes, `show-details=always`
