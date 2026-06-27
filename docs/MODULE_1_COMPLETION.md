# Module 1 Completion Report — Foundation

**Scope:** Identity & authentication, Farm Digital Twin core (land parcels + crop history),
Angular web shell, local Docker Compose environment, CI.

## Features implemented

### user-service
- [x] Registration by phone + password (Indian 10-digit mobile validation)
- [x] Login, returning a 15-minute JWT access token + a 30-day opaque refresh token
- [x] Refresh token rotation/validation, stored only as a SHA-256 hash (never raw)
- [x] Logout (revokes all refresh tokens for the user)
- [x] Authenticated profile lookup (`GET /api/v1/users/me`)
- [x] BCrypt password hashing (cost factor 12)
- [x] Global exception handling with a consistent JSON error shape
- [x] OpenAPI/Swagger UI
- [x] Flyway-managed schema (`users`, `refresh_tokens`)

### farm-twin-service
- [x] One Farm Digital Twin per user (`POST/GET /api/v1/farm-twins/me`)
- [x] Land parcel CRUD (create/list/get), scoped to the caller's own twin
- [x] Crop history records nested under a land parcel, scoped to the caller's own twin
- [x] A simple, transparent profile-completeness scoring heuristic (not ML — see Known
      Limitations)
- [x] JWT verification using the same signing secret as user-service (trust boundary
      between services, not a duplicate identity system)
- [x] Flyway-managed schema (`farm_twins`, `land_parcels`, `crop_history`)

### Frontend (Angular 19)
- [x] Register / Login pages with reactive forms and validation
- [x] Functional HTTP interceptor: attaches JWT, performs one silent refresh-and-retry on 401
- [x] Functional route guard protecting `/dashboard/**`
- [x] Session restoration on app boot via `APP_INITIALIZER`
- [x] Dashboard shell (sidenav layout) + overview page with profile-completeness gauge
- [x] Land parcel list + "add parcel" dialog form
- [x] Signals used for all reactive app state (`AuthService.currentUser`,
      `FarmTwinService.farmTwin`)
- [x] Angular Material theming customized to a domain-appropriate palette (not default
      Material blue)

### Infrastructure
- [x] Dockerfiles (multi-stage, non-root user, healthchecks) for both services
- [x] `docker-compose.yml` running both services + two Postgres instances locally
- [x] GitHub Actions CI: backend (`mvn verify` per service) and frontend (Karma tests +
      dev/prod builds)

## Tests written

- `user-service`: `AuthServiceTest` (6 cases), `JwtTokenProviderTest` (4 cases),
  `AuthFlowIntegrationTest` (full HTTP-layer register→login→refresh→logout flow, 3 cases)
- `farm-twin-service`: `FarmTwinServiceTest` (6 cases), `FarmTwinFlowIntegrationTest`
  (full HTTP-layer twin→parcel→crop-history lifecycle, 2 cases)
- `frontend`: `AuthService`, `authInterceptor`, `authGuard`, `FarmTwinService` spec files
  (Jasmine/Karma)

## Verification status — update: now genuinely verified by CI

The table below was accurate at the time this module was first written, before any CI run
had ever executed against this backend. It's kept for context on how this was developed,
but the bottom line as of commit `05086c4` is: **both backend services and the frontend
pass their full automated test suites in GitHub Actions.** See the
[Actions tab](../../../actions) for live status.

| Component | What I verified | How |
|---|---|---|
| Frontend (Angular) | **Full TypeScript compile + production bundle build succeeded** | `ng build` (dev and prod configs), both passed |
| Frontend tests | Type-checked successfully | `tsc --noEmit` against the spec files — could not execute them (no headless Chrome reachable in this sandbox; will run for real in GitHub Actions on push) |
| Backend (Java/Maven) | **Could not compile or run** | My sandbox's network allowlist does not include Maven Central, so `mvn compile`/`mvn test` cannot resolve dependencies here at all. I checked package declarations, brace balance, and DTO/entity field alignment by hand instead. |

**What actually happened next:** the first real GitHub Actions run against this backend
failed — both services. Three real, distinct bugs were found and fixed across several
follow-up commits, each diagnosed from actual CI failure logs (captured via a
`ci-diagnostics` branch, since GitHub's standard log/artifact download redirects to Azure
Blob Storage, which wasn't reachable from the sandbox used to build this):

1. **401 vs 403 on unauthenticated requests** — Spring Security's default behavior for a
   request with no credentials at all is `403 Forbidden`, which is misleading; REST
   convention (and this project's own integration tests) correctly expect `401
   Unauthorized` for "no token was sent." Fixed by adding an explicit
   `AuthenticationEntryPoint` in both services' `SecurityConfig`.
2. **Exceptions were being silently swallowed** — the generic `@ExceptionHandler(Exception.class)`
   in both services returned a 500 with no detail and never logged the underlying
   exception, which made the *next* bug far harder to diagnose than it should have been.
   Fixed by logging the full exception with stack trace before responding.
3. **Unnamed `@PathVariable` arguments** — `@PathVariable UUID parcelId` relied on the
   compiler preserving parameter names via reflection (the `-parameters` flag), which
   Maven doesn't enable by default. This silently broke the two *nested* endpoints under
   `/land-parcels/{parcelId}/...` while the non-nested endpoints worked fine, which is why
   it wasn't caught by manual code review. Fixed by adding `-parameters` to the compiler
   plugin and, more robustly, adding explicit `@PathVariable("name")` everywhere.

This is exactly the kind of thing that manual review without a compiler cannot reliably
catch — which is the whole reason this module's verification status was written so
explicitly in the first place, rather than just asserting "tested and working."



## Known limitations / deliberate scope cuts

- **JWT trust model is a shared HMAC secret** between the two services. This is explicitly
  flagged in code comments as a temporary simplification — the real fix (RS256 + a JWKS
  endpoint on user-service) is deferred to a later module so farm-twin-service never has to
  hold a token-*issuing* secret.
- **Land parcel geography is a lat/lng centroid**, not a PostGIS polygon. Upgrading this
  is an additive migration, not a breaking one, whenever PostGIS is provisioned.
- **Profile completeness score is a plain weighted heuristic** (parcels present / crop set /
  history logged → 40/30/30), explicitly not a trained model. The blueprint's "Farm Health
  Score" implies something more sophisticated; that's a later, data-dependent module.
- **No Neo4j, Kafka, Redis, Pinecone** — none of Module 1's features need them yet.
- **No rate limiting** on auth endpoints yet (brute-force protection) — flagged for the
  security-hardening pass before this goes anywhere near production traffic.

## Remaining work (tracked for later modules)

See [`docs/ROADMAP.md`](ROADMAP.md) for the full sequencing. Immediate next candidates:
rate limiting + account lockout on auth, an API Gateway in front of both services, and the
first real AI service (FastAPI) once there's a concrete prediction target with real
(even if small) training data behind it.
