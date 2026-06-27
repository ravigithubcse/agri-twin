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

## Verification status — please read this carefully

I built and reviewed this code carefully, but my ability to **execute** it was limited by
my sandbox's network policy, and I want to be precise about what that means rather than
imply more confidence than is warranted:

| Component | What I verified | How |
|---|---|---|
| Frontend (Angular) | **Full TypeScript compile + production bundle build succeeded** | `ng build` (dev and prod configs), both passed |
| Frontend tests | Type-checked successfully | `tsc --noEmit` against the spec files — could not execute them (no headless Chrome reachable in this sandbox; will run for real in GitHub Actions on push) |
| Backend (Java/Maven) | **Could not compile or run** | My sandbox's network allowlist does not include Maven Central, so `mvn compile`/`mvn test` cannot resolve dependencies here at all. I checked package declarations, brace balance, and DTO/entity field alignment by hand instead. |

**What this means practically:** the backend code has not been executed by anyone yet,
including me. It is my best work and I checked it as carefully as I could without a
compiler, but "compiles and passes tests" for the Java side is a claim that will only be
true once CI runs on push, or once you run `mvn verify` locally. Please treat the backend
as "ready for its first real compile," not as "tested." I'd recommend running the backend
test suite locally or watching the GitHub Actions run before building on top of it.

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
