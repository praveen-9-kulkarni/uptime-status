# 019 — Stale check state on the status page

## Goal

Treat “last check too old” as its own visibility state: derive UP / DOWN / STALE / NO_DATA from persisted results and probe timing, show stale rows and summary counts on `/status`, and keep the rule testable with a fixed clock.

## Decisions

### Where stale vs up/down is computed

- **Options:** Template/Thymeleaf only; `StatusPageController`; `TargetService`.
- **Chose:** `TargetService.stateOf(CheckResult)` plus a small `State` enum.
- **Why:** One rule for API and UI later; threshold uses the same probe-interval settings as scheduling.
- **Consequences:** Controller calls `stateOf` and only formats age text when state is `STALE`.

### Stale threshold formula

- **Options:** Fixed duration property; multiples of `uptime.probe-interval-ms` plus grace.
- **Chose:** `staleAfterIntervals * probeIntervalMs + staleGraceMs` via `uptime.stale-after-intervals` and `uptime.stale-grace-ms` (defaults 2 and 10000).
- **Why:** Stale tracks missed probe cadence without duplicating interval config.
- **Consequences:** Changing probe interval changes stale boundary unless intervals/grace are adjusted.

### Time source for probes and staleness

- **Options:** `Instant.now()`; injectable `java.time.Clock`.
- **Chose:** `Clock` in `TargetService` and `StatusPageController` (with `DateTimeConfig` bean for the app).
- **Why:** Unit tests can fix “now” and assert boundary at 130_000 ms (2×60_000 + 10_000).
- **Consequences:** Constructors and tests construct `TargetService` with explicit clock and config values.

### Status page presentation

- **Options:** Keep pill logic on `lastCheck.up` only; drive pills and summary from `State`.
- **Chose:** `StatusRow` carries `state` and optional `ageText`; summary counts UP/DOWN/STALE/NO_DATA; stale pill shows age and “Last result: Up/Down”.
- **Why:** Stale is not the same as down; operators see how old the snapshot is.
- **Consequences:** New CSS tokens for stale pill and summary stat coloring.

## Proof

not recorded

## Code state

- **Ref:** `61e7540` (main) + unstaged working tree
- **Files:** `StatusPageController.java` (+24/−2), `TargetService.java` (+38/−3), `application.properties` (+2), `app.css` (+14), `status.html` (+25/−7), `TargetServiceTest.java` (+50/−1), `test/application.properties` (+4/−1); new `DateTimeConfig.java`, `enums/State.java` (untracked in context)
