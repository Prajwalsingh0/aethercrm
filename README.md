# AetherCRM

**Intelligent Customer Relationships, Elevated** · **v1 complete**

Multi-tenant AI-native CRM: sales, support, marketing, knowledge, workflows, email, audit, and an **AI Command Center** that can execute actions.

## What makes AetherCRM different

- **AI Command Center** — not chat-only: confirmable actions that create tasks, rescore leads, log email, qualify high-score leads
- **Explainable lead scoring** — transparent factors (email, title/decision-maker, revenue INR, WhatsApp/referral source)
- **Smart insights** — hot leads + actionable warnings on the dashboard
- **Tenant audit trail** — who changed what, including AI actions
- **INR-native defaults** — currency and locale oriented for India / MSME demos
- **Multi-tenant JWT isolation** — real SaaS tenancy, not a single-user demo

See **[DEMO.md](DEMO.md)** for a 5-minute walkthrough.

## Quick start (local H2)

```bash
cd backend && mvn spring-boot:run
# other terminal
cd frontend && npm install && npm run dev
```

- App: http://localhost:5173
- Swagger: http://localhost:8080/swagger-ui.html

## Docker (Postgres)

```bash
docker compose up --build
```

## Email (SMTP)

By default emails are **logged only** (stored in CRM + activity timeline).

```bash
export MAIL_ENABLED=true
export MAIL_HOST=smtp.example.com
export MAIL_PORT=587
export MAIL_USERNAME=you@example.com
export MAIL_PASSWORD=secret
export MAIL_FROM=crm@yourcompany.com
```

## Features

- Multi-tenant auth (JWT + refresh)
- Leads (convert + rescore), Accounts, Contacts
- Sales pipeline + stage history
- Products catalog
- Activities (tasks/calls/meetings/notes)
- Support tickets + comments
- Marketing campaigns
- Knowledge base articles
- Workflow definitions
- Email (SMTP send + log-only mode, activity link)
- AI Command Center (chat + executable actions)
- Explainable lead scoring + rescore API
- Smart insights dashboard
- Audit trail
- AI Copilot (mock + OpenAI-compatible)
- Flyway V1–V7, Docker Compose

## Stack

React + TypeScript + Vite + Tailwind · Java 17 · Spring Boot 3.3 · JWT · H2/Postgres · Flyway · JavaMail

## License

MIT (or your choice)
