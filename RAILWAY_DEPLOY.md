# 🚂 Railway Deployment Guide — Agri-Twin AI

## Architecture on Railway
You deploy **4 services** in one Railway project:

```
[PostgreSQL Plugin: users-db]      ← Railway managed DB
[PostgreSQL Plugin: farmtwin-db]   ← Railway managed DB
[user-service]                     ← Spring Boot  :8081
[farm-twin-service]                ← Spring Boot  :8082
[frontend]                         ← Angular + Nginx :80
```

---

## Step 1 — Create Railway Project

1. Go to [railway.app](https://railway.app) → **New Project**
2. Name it `agri-twin`

---

## Step 2 — Add PostgreSQL Databases

### users-db
1. Click **+ New** → **Database** → **PostgreSQL**
2. Rename it `postgres-users`
3. Note the `DATABASE_URL` from its Variables tab

### farmtwin-db
1. Click **+ New** → **Database** → **PostgreSQL**
2. Rename it `postgres-farmtwin`
3. Note the `DATABASE_URL` from its Variables tab

---

## Step 3 — Deploy user-service

1. Click **+ New** → **GitHub Repo** → select `ravigithubcse/agri-twin`
2. Set **Root Directory**: `backend`
3. Set **Dockerfile Path**: `user-service/Dockerfile`
4. Set these **Environment Variables**:

| Variable | Value |
|----------|-------|
| `DB_URL` | `jdbc:postgresql://<HOST>:<PORT>/railway` (convert from DATABASE_URL of postgres-users) |
| `DB_USERNAME` | (from postgres-users Variables → PGUSER) |
| `DB_PASSWORD` | (from postgres-users Variables → PGPASSWORD) |
| `JWT_SECRET` | (generate: `openssl rand -base64 48`) |
| `SERVER_PORT` | `8081` |

> **Tip**: Railway provides `DATABASE_URL` in postgres format. Convert it:
> `postgresql://user:pass@host:port/db` → `DB_URL=jdbc:postgresql://host:port/db`, `DB_USERNAME=user`, `DB_PASSWORD=pass`

5. Click **Deploy** — wait for health check to pass at `/actuator/health`
6. Note the public URL, e.g. `https://user-service-abc123.up.railway.app`

---

## Step 4 — Deploy farm-twin-service

1. Click **+ New** → **GitHub Repo** → select `ravigithubcse/agri-twin`
2. Set **Root Directory**: `backend`
3. Set **Dockerfile Path**: `farm-twin-service/Dockerfile`
4. Set these **Environment Variables**:

| Variable | Value |
|----------|-------|
| `DB_URL` | (from postgres-farmtwin, jdbc format as above) |
| `DB_USERNAME` | (from postgres-farmtwin Variables → PGUSER) |
| `DB_PASSWORD` | (from postgres-farmtwin Variables → PGPASSWORD) |
| `JWT_SECRET` | **MUST BE IDENTICAL** to user-service JWT_SECRET |
| `USER_SERVICE_URL` | `https://user-service-abc123.up.railway.app` |
| `SERVER_PORT` | `8082` |

5. Deploy and wait for health check at `/actuator/health`

---

## Step 5 — Deploy frontend

1. Click **+ New** → **GitHub Repo** → select `ravigithubcse/agri-twin`
2. Set **Root Directory**: `frontend`
3. Set these **Environment Variables**:

| Variable | Value |
|----------|-------|
| `USER_SERVICE_URL` | `https://user-service-abc123.up.railway.app/api/v1` |
| `FARM_TWIN_SERVICE_URL` | `https://farm-twin-service-xyz.up.railway.app/api/v1` |

4. Deploy — health check is at `/health`
5. Note the public URL — this is your app's main URL

---

## Step 6 — Update CORS (after you have all URLs)

Once you have the actual frontend Railway URL (e.g. `https://agri-twin-frontend-abc.up.railway.app`),
add it to the CORS allowed origins in both backend services' `SecurityConfig.java` if it doesn't
match `*.up.railway.app`. Currently `*.up.railway.app` and `*.railway.app` are allowed.

---

## Step 7 — Verify End-to-End

Test via Swagger UI:

- `https://user-service-abc.up.railway.app/swagger-ui.html`
- `https://farm-twin-service-xyz.up.railway.app/swagger-ui.html`

Or run this quick smoke test:
```bash
# Register
curl -X POST https://user-service-abc.up.railway.app/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"phone":"9999999999","password":"Test@1234","fullName":"Ravi Test","stateCode":"KA"}'

# Login (get accessToken)
curl -X POST https://user-service-abc.up.railway.app/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"phone":"9999999999","password":"Test@1234"}'

# Create farm twin (replace TOKEN)
curl -X POST https://farm-twin-service-xyz.up.railway.app/api/v1/farm-twins/me \
  -H "Authorization: Bearer TOKEN"
```

---

## Environment Variable Quick Reference

### user-service
| Variable | Required | Notes |
|----------|----------|-------|
| `DB_URL` | ✅ | `jdbc:postgresql://host:port/db` |
| `DB_USERNAME` | ✅ | PostgreSQL user |
| `DB_PASSWORD` | ✅ | PostgreSQL password |
| `JWT_SECRET` | ✅ | Min 32 chars, same as farm-twin-service |
| `SERVER_PORT` | Optional | Default: 8081 |

### farm-twin-service
| Variable | Required | Notes |
|----------|----------|-------|
| `DB_URL` | ✅ | `jdbc:postgresql://host:port/db` |
| `DB_USERNAME` | ✅ | PostgreSQL user |
| `DB_PASSWORD` | ✅ | PostgreSQL password |
| `JWT_SECRET` | ✅ | **Must match** user-service exactly |
| `USER_SERVICE_URL` | ✅ | Full HTTPS URL of user-service (no trailing slash) |
| `SERVER_PORT` | Optional | Default: 8082 |

### frontend
| Variable | Required | Notes |
|----------|----------|-------|
| `USER_SERVICE_URL` | ✅ | Full HTTPS URL + `/api/v1` |
| `FARM_TWIN_SERVICE_URL` | ✅ | Full HTTPS URL + `/api/v1` |

---

*Built by Ravikumar · Ravi Future Labs · rn5127610@gmail.com*
