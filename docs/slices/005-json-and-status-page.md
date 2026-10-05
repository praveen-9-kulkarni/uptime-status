File:
  Suggested: docs/slices/005-json-and-status-page.md

Slice note:
---
# 005 — JSON last-check and Thymeleaf status table

## Goal
Read last check over HTTP without probing: JSON for one slug, and a catalog table on `/status`. Never-probed is empty/dashes, not 404 or DOWN.

## Decisions
### How to map unknown slug on the API
- Options: try/catch + `ResponseStatusException` in the controller vs `@RestControllerAdvice`
- Chose: `ApiExceptionHandler` → 404 + fixed `{"error":"Target not found"}`; controller only calls `lastCheck`
- Why: API vs UI handlers stay separate; do not leak `ex.getMessage()`; Thymeleaf can get its own advice later
- Consequences: `@RestControllerAdvice` does not style HTML errors; a page-level `@ControllerAdvice` is still deferred

### What GET `/targets/{key}` returns
- Options: live `check()` vs `lastCheck()`; 404 when never probed vs 200 with null
- Chose: `lastCheck`; unknown slug 404; never probed 200 with empty/null body
- Why: refresh must not GET GitHub; “no observation” ≠ “no such target”
- Consequences: JSON stays empty until something calls `check` (no POST probe in this slice)

### Status table data
- Options: model attribute named by slug vs list of `StatusRow`; iterate `checkResults` vs catalog ⟕ last check
- Chose: `targetCatalog()` plus `StatusRow(key, target, lastCheck)` list as `rows`
- Why: template must not hardcode `github`; never-probed targets still appear
- Consequences: adding a catalog entry adds a table row without HTML changes

### HTML vs JSON stack
- Options: JSON only vs add Thymeleaf for the first page
- Chose: `spring-boot-starter-thymeleaf`, `@Controller` `StatusPageController` `GET /status`, `th:text` table + minimal cell CSS
- Why: Web MVC already serves JSON; Thymeleaf is for HTML escaping and a view name
- Consequences: no CSS framework; no UI exception handler yet

## Proof
- `curl http://localhost:8080/targets/nope` → `{"error":"Target not found"}`
- `curl http://localhost:8080/targets/github` → empty body (never probed)
- Browser `http://localhost:8080/status` → one GitHub row, up/status/latency/observed as `—`
  (`curl -v` status lines were not pasted; 404 vs 200 inferred from bodies)

## Code state
- Ref: working tree vs `ed74046`
- Files:
  build.gradle                          |  1 +
  .../controller/StatusPageController.java | 38 +
  .../controller/TargetController.java  | 26 +
  .../exception/ApiExceptionHandler.java | 18 +
  .../service/TargetService.java        |  5 +
  src/main/resources/templates/status.html | 36 +
