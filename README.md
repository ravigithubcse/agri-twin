# AGRI-TWIN AI

A Farm Commodity Digital Twin platform for Indian smallholder farmers, cooperatives, and agri-enterprise buyers.

> **Status: Module 1 of N — Foundation.** This repository is being built incrementally,
> module by module. See [`docs/MODULE_1_COMPLETION.md`](docs/MODULE_1_COMPLETION.md) for
> exactly what is and isn't implemented yet, and [`docs/ROADMAP.md`](docs/ROADMAP.md) for
> what's planned next.

## What's real right now

- **`user-service`** (Java 21 / Spring Boot 3): registration, login, JWT access + refresh
  tokens, logout, profile lookup. Backed by PostgreSQL + Flyway.
- **`farm-twin-service`** (Java 21 / Spring Boot 3): one Farm Digital Twin per user, land
  parcels, crop history records, with ownership enforced on every query. Verifies JWTs
  issued by user-service; does not issue its own.
- **`frontend`** (Angular 19, standalone components, Signals, Angular Material): register,
  login, dashboard overview with a profile-completeness score, and land parcel management.
- Docker Compose to run the whole backend stack locally against real Postgres databases.
- CI (GitHub Actions) that builds and tests every service on every push.

## What's explicitly NOT built yet

Kafka, Redis, Neo4j, Pinecone, the AI/ML prediction services, Flutter mobile app, blockchain
traceability, hedging/insurance integrations, Aadhaar/government data integrations, and
billing (Razorpay) are all out of scope for Module 1. They depend on infrastructure,
managed-service accounts, or real-world datasets that don't exist yet and are deliberately
sequenced into later modules — see the roadmap.

## Repository layout

```
agri-twin/
├── backend/
│   ├── user-service/          # Auth, identity, RBAC
│   └── farm-twin-service/     # Farm digital twin, land parcels, crop history
├── frontend/                  # Angular 19 web app
├── docker/                    # docker-compose.yml + .env.example for local dev
├── docs/                      # Module completion reports, roadmap, API docs
└── .github/workflows/         # CI for backend and frontend
```

## Running locally

### Backend (requires Docker)

```bash
cd docker
cp .env.example .env   # then edit JWT_SECRET to a real value
docker compose up --build
```

- `user-service` → http://localhost:8081 (Swagger UI at `/swagger-ui.html`)
- `farm-twin-service` → http://localhost:8082 (Swagger UI at `/swagger-ui.html`)

### Frontend

```bash
cd frontend
npm install
npm start   # serves on http://localhost:4200, expects the backend running per above
```

### Running tests

```bash
# Backend (each service)
cd backend && mvn -pl user-service -am verify
cd backend && mvn -pl farm-twin-service -am verify

# Frontend
cd frontend && npm test
```

## Important context for anyone picking this up

This project began from a comprehensive business blueprint describing a much larger
platform (satellite imagery, AI yield/income prediction, hedging, blockchain traceability,
multi-language voice IVR, etc.). That blueprint is a product vision, not a literal build
spec — many of its numeric claims (model accuracy, dataset sizes) describe outcomes that
require real data collection and managed infrastructure this repository does not yet have.
Each module's completion report is explicit about what's genuinely implemented versus
deferred, so nothing here should be assumed "production ready" beyond what its own
documentation claims.
