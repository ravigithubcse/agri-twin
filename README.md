<div align="center">

# 🌾 AGRI-TWIN AI

**Farm Commodity Digital Twin — India's Smallholder Farmer Intelligence Platform**

[![Java 21](https://img.shields.io/badge/Java_21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://docs.oracle.com/en/java/javase/21/)
[![Spring Boot 3](https://img.shields.io/badge/Spring_Boot_3-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://docs.spring.io/spring-boot/docs/3.2.x/reference/html/)
[![Angular 19](https://img.shields.io/badge/Angular_19-DD0031?style=for-the-badge&logo=angular&logoColor=white)](https://angular.dev/overview)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/docs/16/)
[![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://docs.docker.com/)
[![GitHub Actions](https://img.shields.io/badge/CI-GitHub_Actions-2088FF?style=for-the-badge&logo=githubactions&logoColor=white)](https://docs.github.com/en/actions)

> A Farm Commodity Digital Twin platform for Indian smallholder farmers, cooperatives, and agri-enterprise buyers —
> predicts yield, income, and risk using real-time IoT, satellite imagery, and AI.

[![View Repo](https://img.shields.io/badge/View%20Repo-1565C0?style=for-the-badge&logo=github&logoColor=white)](https://github.com/ravigithubcse/agri-twin)
[![Module 1](https://img.shields.io/badge/Module_1-Foundation-22c55e?style=for-the-badge)](docs/MODULE_1_COMPLETION.md)
[![Roadmap](https://img.shields.io/badge/Roadmap-View-818CF8?style=for-the-badge)](docs/ROADMAP.md)

</div>

---

## 🏗️ Architecture

```mermaid
flowchart TB
    subgraph FE["🌐 Frontend — Angular 19"]
        direction LR
        F1["📋 Register / Login"]
        F2["🏠 Dashboard\n(Profile Score)"]
        F3["🗺️ Land Parcel\nManagement"]
        F4["📊 Crop History\nRecords"]
    end

    subgraph GW["🔀 API Layer (Docker Compose)"]
        G1["🔐 user-service  :8081\nJWT Issue · Auth · RBAC"]
        G2["🌿 farm-twin-service  :8082\nDigital Twin · Parcels · Crops"]
    end

    subgraph DB["🗄️ Data Layer"]
        D1["🐘 PostgreSQL\nusers_db\n(Flyway migrations)"]
        D2["🐘 PostgreSQL\nfarm_twin_db\n(Flyway migrations)"]
    end

    subgraph CI["⚙️ CI — GitHub Actions"]
        C1["Build + Test\nuser-service"]
        C2["Build + Test\nfarm-twin-service"]
        C3["Build + Test\nfrontend"]
    end

    subgraph ROADMAP["🔮 Planned — Future Modules"]
        direction LR
        R1["📡 Kafka\nEvent Streaming"]
        R2["⚡ Redis\nCaching"]
        R3["🛰️ Satellite\nImagery AI"]
        R4["🤖 Yield / Income\nML Prediction"]
        R5["📱 Flutter\nMobile App"]
        R6["⛓️ Blockchain\nTraceability"]
    end

    FE -->|HTTP / JWT Bearer| GW
    G1 <-->|Flyway + JPA| D1
    G2 <-->|Flyway + JPA| D2
    G2 -->|Verifies JWT issued by| G1
    CI -.->|every push| GW

    classDef fe fill:#0d47a1,stroke:#42a5f5,color:#e3f2fd
    classDef gw fill:#1b5e20,stroke:#66bb6a,color:#e8f5e9
    classDef db fill:#3e2723,stroke:#ff8a65,color:#fbe9e7
    classDef ci fill:#1a237e,stroke:#7986cb,color:#e8eaf6
    classDef road fill:#212121,stroke:#616161,color:#9e9e9e,stroke-dasharray:5 5
    class F1,F2,F3,F4 fe
    class G1,G2 gw
    class D1,D2 db
    class C1,C2,C3 ci
    class R1,R2,R3,R4,R5,R6 road
```

**Request Flow (Module 1 — what is actually built):**
1. **Angular 19 SPA** (standalone components + Signals + Angular Material) serves register, login, dashboard, and land parcel management
2. All requests carry a **JWT Bearer token** — issued exclusively by `user-service`, verified by both services
3. **user-service** (:8081) handles registration, login, JWT access + refresh token lifecycle, logout, and profile lookup — backed by its own PostgreSQL database with Flyway migrations
4. **farm-twin-service** (:8082) manages one Farm Digital Twin per user, land parcels, and crop history records — enforces ownership on every query, verifies JWTs but never issues them
5. Both services run together via **Docker Compose** against real PostgreSQL databases with a single `docker compose up --build`
6. **GitHub Actions CI** builds and runs tests for all three components on every push to any branch
7. **Future modules** (greyed out) will add Kafka event streaming, Redis caching, satellite imagery AI, ML yield/income prediction, Flutter mobile, and blockchain traceability — sequenced by infrastructure and data availability

---

## ✅ What Is Built (Module 1)

| Component | Status | Details |
|-----------|--------|---------|
| `user-service` | ✅ Done | Registration · Login · JWT access + refresh tokens · Logout · Profile |
| `farm-twin-service` | ✅ Done | Farm Digital Twin · Land parcels · Crop history · Ownership RBAC |
| `frontend` | ✅ Done | Angular 19 · Standalone components · Signals · Dashboard · Profile score |
| Docker Compose | ✅ Done | Full backend stack with real PostgreSQL — single command local dev |
| GitHub Actions CI | ✅ Done | Build + test all services on every push |

## 🔮 What Is Planned (Future Modules)

| Feature | Module | Dependency |
|---------|--------|-----------|
| Apache Kafka event streaming | 2 | Managed Kafka cluster |
| Redis caching layer | 2 | Managed Redis instance |
| AI/ML yield & income prediction | 3 | Real farm datasets + satellite imagery |
| Flutter mobile app | 3 | Core API stability |
| Blockchain traceability | 4 | Managed blockchain infra |
| Aadhaar / govt data integration | 4 | Regulatory approval |
| Razorpay billing | 5 | Business registration |
| Satellite imagery processing | 3 | ISRO / Planet Labs API access |

---

## 📁 Repository Layout

```
agri-twin/
├── backend/
│   ├── user-service/          # Auth · Identity · RBAC
│   └── farm-twin-service/     # Farm Digital Twin · Land Parcels · Crop History
├── frontend/                  # Angular 19 web app
├── docker/                    # docker-compose.yml + .env.example
├── docs/                      # Module completion reports · Roadmap · API docs
└── .github/workflows/         # CI — backend & frontend
```

---

## 🚀 Running Locally

### Backend (requires Docker)

```bash
cd docker
cp .env.example .env    # edit JWT_SECRET to any strong secret
docker compose up --build
```

| Service | URL | Swagger |
|---------|-----|---------|
| user-service | http://localhost:8081 | http://localhost:8081/swagger-ui.html |
| farm-twin-service | http://localhost:8082 | http://localhost:8082/swagger-ui.html |

### Frontend

```bash
cd frontend
npm install
npm start   # → http://localhost:4200  (backend must be running)
```

### Tests

```bash
# Backend services
cd backend && mvn -pl user-service -am verify
cd backend && mvn -pl farm-twin-service -am verify

# Frontend
cd frontend && npm test
```

---

## 🛠️ Tech Stack

| Layer | Technologies |
|-------|-------------|
| **Backend** | Java 21 · Spring Boot 3 · Spring Security 6 · Spring Data JPA · Flyway |
| **Frontend** | Angular 19 · Standalone Components · Signals · Angular Material · TypeScript |
| **Database** | PostgreSQL (per service) · Flyway migrations |
| **Auth** | JWT access tokens + refresh tokens · RBAC ownership enforcement |
| **DevOps** | Docker · Docker Compose · GitHub Actions CI |
| **Planned** | Apache Kafka · Redis · PyTorch · FastAPI · Flutter · Neo4j · Pinecone |

---

<div align="center">

*Built by **Ravikumar** — Bengaluru, India 🇮🇳 · Ravi Future Labs*

[![View Repo](https://img.shields.io/badge/View%20Repo-1565C0?style=for-the-badge&logo=github&logoColor=white)](https://github.com/ravigithubcse/agri-twin)

</div>
