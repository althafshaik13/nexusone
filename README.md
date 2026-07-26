# NexusOne

An AI-powered enterprise support & operations platform, built as a portfolio
project demonstrating production-grade Java + Angular + AI engineering.

## Repos in this org
- gateway              — Spring Cloud Gateway, single entry point
- auth-service         — Keycloak-backed auth, JWT, MFA, RBAC
- ticket-service       — Core domain: ticket lifecycle, outbox pattern
- notification-service — Kafka/RabbitMQ-driven notification dispatch
- frontend             — Angular 22 SPA (customer/agent/admin consoles)
- infrastructure       — docker-compose, k8s manifests, Helm charts

## Build phases
See docs/ROADMAP.md.
v1 (this scaffold) = Auth + Ticket + Notification + Gateway + Frontend shell.
Everything else (Workflow Engine, AI Copilot, Search, Analytics, Audit,
Admin Panel) is v2+ — do not start those until v1 runs end-to-end and is
fully demoable.

## Local dev
Requires Docker Desktop (not installed as of this scaffold — install it first).
```
cd infrastructure
docker compose up -d
```
Brings up Postgres, Redis, Kafka (KRaft mode, no ZooKeeper), Keycloak, RabbitMQ.
