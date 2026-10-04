# AetherCRM

**Intelligent Customer Relationships, Elevated**

Multi-tenant AI-native CRM: sales, support, marketing, knowledge, workflows, and an AI copilot.

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

Live AI (optional):
```bash
export AI_PROVIDER=openai
export OPENAI_API_KEY=sk-...
docker compose up --build
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
- AI Copilot (mock + OpenAI-compatible)
- Flyway V1–V5, Docker Compose

## Stack

React + TypeScript + Vite + Tailwind · Java 17 · Spring Boot 3.3 · JWT · H2/Postgres · Flyway
