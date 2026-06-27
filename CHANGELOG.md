# Changelog

All notable changes to this project are documented here.
Format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
versioning follows [Semantic Versioning](https://semver.org/).

## [0.1.0] — Module 1: Foundation

### Added
- `user-service`: registration, login, JWT access/refresh tokens, logout, profile lookup
- `farm-twin-service`: Farm Digital Twin, land parcels, crop history, ownership-scoped queries
- Angular 19 frontend: auth pages, dashboard shell, farm twin overview, land parcel management
- Docker Compose for local backend stack (two Postgres instances + both services)
- GitHub Actions CI for backend (per-service `mvn verify`) and frontend (Karma + builds)
- Unit and integration test suites for both backend services and core frontend services

### Known limitations
See `docs/MODULE_1_COMPLETION.md` for the full, explicit list — most notably: shared-secret
JWT trust model between services (RS256/JWKS deferred), no rate limiting yet on auth
endpoints, profile completeness is a heuristic rather than a model.

## [0.1.1] — Module 1: First CI run + fixes

The first real GitHub Actions run against this backend (it had never been compiled before
this point) failed for both services. Fixed in follow-up commits:

### Fixed
- 401 vs 403 on unauthenticated requests (Spring Security's undecorated default is 403;
  added an explicit `AuthenticationEntryPoint` in both services)
- Exceptions were silently swallowed by the generic exception handler with no logging
- Unnamed `@PathVariable` arguments broke the two nested land-parcel/crop-history
  endpoints, relying on a compiler flag (`-parameters`) that wasn't set; fixed both by
  enabling that flag and by naming every `@PathVariable` explicitly

Both backend services and the frontend now pass their full CI suites.
