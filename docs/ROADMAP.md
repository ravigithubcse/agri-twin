# AGRI-TWIN AI — Roadmap

This roadmap sequences the blueprint's full vision into modules that can each be built,
tested, and verified independently, following the blueprint's own "Delivery Strategy"
principle: one complete module at a time, never moving on until the current one compiles,
runs, and is tested.

## Module 1 — Foundation ✅ (this delivery)
Identity/auth (user-service), Farm Digital Twin core (farm-twin-service), Angular web
shell, Docker Compose, CI. See `MODULE_1_COMPLETION.md`.

## Module 2 — Hardening & Gateway (proposed next)
- Rate limiting + account lockout on auth endpoints
- API Gateway (Spring Cloud Gateway) in front of user-service + farm-twin-service
- Centralized logging/correlation IDs across services
- Move JWT trust model from shared-secret HS256 to RS256 + JWKS

## Module 3 — Crop & Market Reference Data
- A real (even if initially small/manually curated) crop knowledge base — varieties,
  typical sowing/harvest windows by region, input cost baselines
- Mandi/market price reference data ingestion (this requires picking a real, accessible
  data source — e.g. Agmarknet — and is a research task before it's a coding task)

## Module 4 — First Real AI Service
- A FastAPI service with one well-scoped, honestly-evaluated model (e.g. expected yield
  range for a given crop/region/season using whatever real reference data Module 3
  established), reporting calibrated uncertainty and validation metrics — not a fixed
  "accuracy" claim
- MLflow model registry, basic retraining pipeline

## Module 5 — Cooperative & Enterprise Personas
- Cooperative admin role: aggregate view across member farmers
- Enterprise buyer role: sourcing/demand visibility (read-only against aggregated,
  anonymized data — needs a privacy design pass before any implementation)

## Later / infrastructure-gated modules (not yet sequenced in detail)
- Satellite imagery ingestion + computer vision (needs a satellite data provider account
  and budget decision)
- Blockchain traceability layer (needs a concrete choice of chain/ledger and a real reason
  a database with an audit log wouldn't suffice first)
- Flutter mobile app
- Voice/IVR and WhatsApp-first UX for low-literacy users (this is core to the blueprint's
  actual target user and should likely move up in priority once Module 1-3 are stable —
  worth revisiting this ordering with stakeholders)
- Payments/billing (Razorpay), Aadhaar/government data integrations — both need compliance
  review (KYC, DPDP Act) before implementation starts, not just API credentials

## Principle for all future modules

Each module ships with: working code, real tests that run in CI, a completion report that
states plainly what's implemented vs. deferred, and no claimed metric (accuracy, latency,
uptime) that hasn't actually been measured against real running code.
