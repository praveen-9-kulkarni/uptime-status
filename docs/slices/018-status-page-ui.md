# 018 — Status page UI

## Goal

Give the HTML status and history views a consistent, readable layout (shared styling, branding, up/down affordances) without blocking static assets behind Spring Security.

## Decisions

### Where to put styles

- **Options:** Keep per-page `<style>` blocks; add a shared stylesheet under `static/`; pull in a CSS framework.
- **Chose:** Shared `static/css/app.css` linked from both Thymeleaf templates.
- **Why:** One place to style status and history; matches Spring Boot static resource conventions.
- **Consequences:** Templates depend on `/css/**` being reachable; security rules must allow those paths.

### Static assets and security

- **Options:** Leave only `/favicon.ico` permitted; permit SVG favicon and CSS paths explicitly; authenticate everything except HTML.
- **Chose:** `permitAll` for `/favicon.ico`, `/favicon.svg`, and `/css/**` in `SecurityConfig`.
- **Why:** Browsers request favicon and stylesheet on GET without session; blocked assets would break the new UI behind security.
- **Consequences:** Public read-only static files; no change to API/auth posture for app routes.

### Status presentation

- **Options:** Plain table cells for up/down; colored pills and summary counts; empty-state rows.
- **Chose:** Pills (`Up` / `Down` / `No data`), header summary (target/up/down counts on status; recent check count on history), empty rows when catalog or history is empty, clickable target URLs with `rel="noopener noreferrer"`.
- **Why:** Faster scanning of fleet health; history page title uses `${target.name}` instead of a bare key.
- **Consequences:** Slightly more Thymeleaf (e.g. collection filters for up/down counts).

### Page chrome

- **Options:** Minimal link back on history only; shared top bar and page structure on both pages.
- **Chose:** Shared topbar brand (“Uptime status” / “Catalog probes”), viewport meta, SVG favicon link on both pages.
- **Why:** History and status feel like one app; reasonable on small screens.
- **Consequences:** Duplicate header markup in two templates until a layout fragment exists (not introduced in this slice).

## Proof

not recorded

## Code state

- **Ref:** `d99666a` on `main`, with local uncommitted work (not staged).
- **Files:** `git diff --stat` — `SecurityConfig.java` (+1 matcher line), `status-history.html`, `status.html` (133 insertions, 65 deletions). Untracked: `src/main/resources/static/` (`css/app.css`, `favicon.svg` per `git status`).
