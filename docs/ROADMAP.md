# Roadmap (v1 — build in this order, one milestone = one PR)

1. Ticket Service domain model + Postgres schema (Flyway migration)
2. Ticket Service CRUD + state machine, no auth yet
3. Dockerize Ticket Service; docker-compose local Postgres
4. Angular app shell — routing, layout, environments
5. Angular ticket list/detail view against Ticket Service
6. Keycloak realm + roles (customer/agent/admin)
7. Spring Security JWT resource server on Ticket Service
8. Angular OAuth2/PKCE login + HTTP interceptor
9. Route guards + role-based UI rendering
10. MFA (TOTP) for admin role
11. Kafka setup; ticket.created / ticket.status.changed producers
12. Outbox pattern (transactional event publish)
13. Notification Service skeleton — Kafka consumer, email dispatch
14. RabbitMQ retry/DLQ for failed notifications
15. Spring Cloud Gateway — route all traffic through it
16. Eureka service discovery — remove hardcoded service URLs
17. Rate limiting at the gateway
18. Prometheus + Grafana dashboards per service
19. GitHub Actions CI (build, test, docker build/push)
20. Deploy full v1 to a local kind/minikube cluster

Ship this fully working and demoable before touching v2.
