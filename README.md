# AetherCRM

**Intelligent Customer Relationships, Elevated**

AetherCRM is a next-generation, AI-native CRM platform built as a modular monolith. It unifies sales pipelines, lead management, customer support foundations, and an AI copilot into one multi-tenant SaaS application.

> Active development. See [docs/ROADMAP.md](docs/ROADMAP.md) and [docs/PROGRESS.md](docs/PROGRESS.md).

## Clone

```bash
git clone https://github.com/Prajwalsingh0/aethercrm.git
cd aethercrm
```

## Stack

| Layer    | Technology                                      |
|----------|-------------------------------------------------|
| Frontend | React 19, TypeScript, Vite, Tailwind CSS        |
| Backend  | Java 17, Spring Boot 3.3, Spring Security, JWT  |
| Database | PostgreSQL 16 (H2 for local demo) + Flyway      |
| AI       | Provider-agnostic layer (Mock + OpenAI-compatible) |

## Quick Start

### Backend

```bash
cd backend
# Uses H2 file DB by default — no external DB required for demo
mvn spring-boot:run
```

- API: http://localhost:8080
- Swagger: http://localhost:8080/swagger-ui.html
- Health: http://localhost:8080/actuator/health

### Frontend

```bash
cd frontend
npm install
npm run dev
```

App: http://localhost:5173

### First user

1. Open the app → **Register**
2. Create an organization (e.g. slug `acme`)
3. You become the ADMIN of that tenant
4. Explore Leads, Pipeline, AI Copilot (demo mode)

## Environment

Copy `.env.example` and set values as needed. Never commit real secrets.

| Variable         | Description                     | Default |
|------------------|---------------------------------|---------|
| `JWT_SECRET`     | ≥256-bit secret for JWT         | (dev)   |
| `DATABASE_URL`   | JDBC URL                        | H2 file |
| `AI_PROVIDER`    | `mock` or `openai`              | `mock`  |
| `OPENAI_API_KEY` | Required when AI_PROVIDER=openai | —      |

## Features implemented

- Multi-tenant identity (org, users, roles, JWT + refresh rotation)
- Leads CRUD, search, rule-based scoring, convert → contact + account
- Accounts & Contacts CRUD
- Sales pipeline with stages, opportunities, stage-move audit history
- Live dashboard metrics
- Demo AI copilot (permission-aware, mock provider)

## Architecture

See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).

## License

For demonstration and evaluation purposes.
