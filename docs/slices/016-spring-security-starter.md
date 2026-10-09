# 016 — Spring Security starter

## Goal

Bring Spring Security onto the uptime-status service so HTTP access can be governed explicitly (dependency on the classpath and configuration under `config/`).

## Decisions

### Add Spring Security now vs. leave the app open

- **Options:** Add `spring-boot-starter-security` and a `SecurityConfig` (or equivalent) in this slice; defer security until auth requirements are defined.
- **Chose:** Add the starter in `build.gradle` and introduce `src/main/java/com/project/uptime_status/config/` (untracked at slice time).
- **Why:** Not recorded in slice context.
- **Consequences:** Spring Security auto-configuration applies once the app runs with these changes; exact permit/deny rules live in the new config class (not captured in the diff excerpt).

## Proof

not recorded

## Code state

- **Ref:** `dbcaac1` on `main`, with local uncommitted changes (`main...origin/main`).
- **Files:**
  - `build.gradle` — `+1` line: `implementation 'org.springframework.boot:spring-boot-starter-security'`
  - `src/main/java/com/project/uptime_status/config/` — untracked (includes security configuration; contents not in `git diff`)
