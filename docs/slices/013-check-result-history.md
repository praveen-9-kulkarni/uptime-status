File:
  Suggested: docs/slices/013-check-result-history.md

Slice note:
---
# 013 — Check result history

## Goal
Persist every probe outcome (not only the latest row) and expose a per-target history view on the status UI so operators can see recent checks.

## Decisions
### Where to store history
- Options: append-only history table vs extending the existing last-check row (e.g. JSON blob or in-place only)
- Chose: separate `check_result_history` (Flyway V2) plus unchanged last-check upsert on `check_result`
- Why: keep “last check” fast and simple while supporting ordered “recent N” reads per slug
- Consequences: every successful `check` dual-writes; history grows unbounded until a later retention/TTL slice

### How to expose history in the UI
- Options: dedicated page vs embedding history on the main status table
- Chose: `GET /status/{key}/history` → `status-history` template, last 20 rows; “History” link on the status page Actions column
- Why: main status page stays a catalog snapshot; history is drill-down per target
- Consequences: history limit (20) is fixed in the controller call to `recentHistory`; pagination/filtering deferred

### Local probe cadence
- Options: keep `uptime.probe-interval-ms=60000` vs slower interval
- Chose: `300000` (5 minutes) in `application.properties`
- Why: not recorded in attached context
- Consequences: scheduled probes run less often in default local config; revert or env-specific tuning if that was only for manual testing

## Proof
not recorded

## Code state
- Ref: working tree vs `2ae17a9`
- Files:
  .../controller/StatusPageController.java | 11 ++++++ .../uptime_status/service/TargetService.java | 40 +++++++++++++++++++++- src/main/resources/application.properties | 2 +- src/main/resources/templates/status.html | 3 +- .../uptime_status/service/TargetServiceTest.java | 13 +++++++ 5 files changed, 66 insertions(+), 3 deletions(-)
  Untracked (per `git status`, not in `--stat` above):
    - `CheckResultHistoryEntity.java`, `CheckResultHistoryRepository.java`
    - `db/migration/V2__check_result_history.sql`
    - `templates/status-history.html`