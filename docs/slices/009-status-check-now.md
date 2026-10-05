File:
  Suggested: docs/slices/009-status-check-now.md

Slice note:
---
# 009 — Check now on the status page

## Goal
Trigger an immediate probe from `/status` and land back on the table with an updated last check. No Spring Security / CSRF yet. JSON `POST /targets/{key}/check` unchanged.

## Decisions
### Where the form posts
- Options: form → `POST /targets/{key}/check` (JSON body in the browser) vs page `POST` + redirect
- Chose: `POST /status/{key}/check` on `StatusPageController` → `check(key)` → `redirect:/status`
- Why: status UX should stay HTML; reuse the same `check` service method
- Consequences: two POST entry points (page vs JSON API); `@RestControllerAdvice` still does not cover HTML errors for unknown keys (forms only use catalog keys)

### Verb
- Options: GET link “check” vs POST form
- Chose: `<form method="post">` + button
- Why: probe is a side effect; browsers must not prefetch it
- Consequences: CSRF tokens deferred until Spring Security; unauthenticated local dashboard ships without a token

## Proof
Not pasted in thread. Intended: open `/status`, click Check now → 302 to `/status`, row shows a new `observedAt` / latency; GET `/status` still does not probe by itself.

## Code state
- Ref: working tree vs `1abfbf2`
- Files:
  .../controller/StatusPageController.java | 8 +
  src/main/resources/templates/status.html | 6 +