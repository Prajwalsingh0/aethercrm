# AetherCRM

**Intelligent Customer Relationships, Elevated**

AetherCRM is a next-generation, AI-native CRM platform built as a modular monolith. It unifies sales pipelines, lead management, customer support, activities, and an AI copilot into one multi-tenant SaaS application.

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
mvn spring-boot:run
```

- API: http://localhost:8080
- Swagger: http://localhost:8080/swagger-ui.html

### Frontend

```bash
cd frontend
npm install
npm run dev
```

App: http://localhost:5173

### First user

1. Open the app → **Register**
2. Create an organization
3. Explore Leads, Pipeline, Activities, Support, AI Copilot

## Features

- Multi-tenant identity (org, users, roles, JWT + refresh rotation)
- Leads CRUD, convert → contact + account + opportunity
- Accounts & Contacts CRUD
- Sales pipeline with stages and stage-move audit history
- Activities (tasks, calls, meetings, notes) with complete action
- Support tickets with status workflow and comments
- Notifications API
- Live dashboard metrics (leads, pipeline, tickets, tasks)
- Demo AI copilot (permission-aware, mock provider)

## License

For demonstration and evaluation purposes.
