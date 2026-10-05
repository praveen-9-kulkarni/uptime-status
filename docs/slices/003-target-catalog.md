# 003 — Hardcoded target catalog

## Goal
One known URL in a record + map, looked up by a stable slug, with a domain exception for unknown keys. No HTTP client, persistence, or API yet.

## Decisions
### Where the catalog lives
- Options: separate `TargetCatalog` `@Component` vs nested `Target` record + map inside the service (weather-insights cities style)
- Chose: `TargetService` with nested `Target` and `TARGETS` map; public `getTarget`, private `resolveTargetOrThrow`
- Why: one consumer for now; split only when web and worker both need the map without HTTP
- Consequences: later check/persist methods call the private helper; callers outside the class use `getTarget`

### Lookup key vs database id
- Options: wait for a Postgres-generated id vs a config slug as the map key
- Chose: slug `"github"` (not a JPA `@Id`)
- Why: catalog is config this slice; DB surrogate ids belong to persisted check results later
- Consequences: observations will key off the same slug (or a FK) when persistence lands

### Missing key
- Options: return null / `Optional` vs domain exception
- Chose: `UnknownTargetException`
- Why: missing target is a domain failure, not a null to leak into HTTP later
- Consequences: API/UI handlers must map it without exposing `ex.getMessage()` to clients

## Proof
`./gradlew test --tests com.project.uptime_status.service.TargetServiceTest --no-daemon` → BUILD SUCCESSFUL  
(known key returns name/url; unknown key throws `UnknownTargetException`; plain `new TargetService()`, no Spring context)

## Code state
- Ref: working tree vs `5fe9cb2`
- Files:
  .../exception/UnknownTargetException.java | 10 +
  .../service/TargetService.java            | 32 +
  .../service/TargetServiceTest.java        | 27 +
