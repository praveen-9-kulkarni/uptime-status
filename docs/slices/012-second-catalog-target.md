Slice note:
---
# 012 — Second catalog target

## Goal
Prove the catalog, queue, status page, and last-check persistence work for more than one slug without new infrastructure.

## Decisions
### How to add a second target
- Options: another hardcoded `TARGETS` entry vs move catalog to DB/config
- Chose: `"google"` → Google / `https://google.com` in the existing `Map.of`
- Why: scheduler, `/status`, enqueue, and `check_result` already iterate the catalog by slug; DB catalog is a later concern
- Consequences: `checkAll` and scheduled enqueue emit two jobs; Flyway unchanged (second PK row on first check)

### Probe semantics for Google’s 301
- Options: treat redirect as down, follow redirects for final 2xx, or keep status &lt; 400 as up
- Chose: keep existing rule — 301 is up (reachable HTTP answer)
- Why: slice is catalog multiplicity, not changing up/down definition
- Consequences: `/status` may show status 301 for google and 200 for github; both `up = true`

### Unit test for checkAll
- Options: leave `verify(save)` once vs expect two saves
- Chose: `verify(..., times(2))` and assert captured slugs contain `github` and `google`
- Why: default Mockito `verify` is once; two catalog keys call `save` twice
- Consequences: test tracks catalog size; add targets → update expected count/slugs

## Proof
`SELECT * FROM check_result` → rows for `google` and `github`.  
`GET /status` → two table rows with Check now.  
`./gradlew test` green after `times(2)` fix.

## Code state
- Ref: working tree vs `fdfa47a`
- Files:
  .../service/TargetService.java     | 3 ++-
  .../service/TargetServiceTest.java | 9 +++++----
  2 files changed, 7 insertions(+), 5 deletions(-)