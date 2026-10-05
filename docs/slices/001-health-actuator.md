# 001 — Health via Actuator

## Goal
Process serves HTTP and reports liveness, with no domain URLs, Redis, or Postgres.

## Decisions
### How to expose health
- Options: custom `@RestController` `GET /health` vs Spring Boot Actuator
- Chose: `spring-boot-starter-actuator` on top of existing Web MVC
- Why: health is not domain; later Postgres/Redis can plug into the same endpoint. A homemade always-200 controller would stay green while a dependency is down.
- Consequences: contract is `/actuator/health`, not `/health`. No custom health controller in this slice.

## Proof
`./gradlew bootRun` then `curl -v http://localhost:8080/actuator/health`

- HTTP/1.1 200
- `Content-Type: application/vnd.spring-boot.actuator.v3+json`
- Body: `{"groups":["liveness","readiness"],"status":"UP"}`

## Code state
- Ref: working tree vs `fb4e6da`
- Files:
  build.gradle | 1 +
