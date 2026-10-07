File:
  Suggested: docs/slices/014-docker-app-packaging.md

Slice note:
---
014 — Docker app packaging

## Goal
Package the Spring Boot app as a container image and run the probe worker alongside existing Postgres and Redis in Compose, using the worker Spring profile and the same JDBC/Redis settings as local dev.

## Decisions
### Where to store history
- No explicit option forks (A vs B) were recorded in the attached context.
- Working tree adds: `Dockerfile`, .dockerignore, docs/docker-app-packaging.md, and a `worker` service block in compose.yaml (build from repo root, `SPRING_PROFILES_ACTIVE=worker`, health-gated `depends_on` for postgres and redis).

## Proof
not recorded

## Code state
- Ref: working tree vs `437b15a`
- Files:
  compose.yaml | 17 +++++++++++++++++
  1 file changed, 17 insertions(+)
  Untracked (not in diff --stat):
    .dockerignore, Dockerfile, docs/docker-app-packaging.md, .cursor/