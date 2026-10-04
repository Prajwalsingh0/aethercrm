# AetherCRM

**Intelligent Customer Relationships, Elevated**

Multi-tenant AI-native CRM: sales, support, marketing, knowledge, workflows, email, and an AI copilot.

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

To send real mail:
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
- Leads (convert), Accounts, Contacts
- Sales pipeline + stage history
- Products catalog
- Activities (tasks/calls/meetings/notes)
- Support tickets + comments
- Marketing campaigns
- Knowledge base articles
- Workflow definitions
- Email (SMTP send + log-only mode, activity link)
- AI Copilot (mock + OpenAI-compatible)
- Flyway V1–V6, Docker Compose

## Stack

React + TypeScript + Vite + Tailwind · Java 17 · Spring Boot 3.3 · JWT · H2/Postgres · Flyway · JavaMail
