# AetherCRM — Demo Guide (v1 Complete)

## Run locally

```bash
git clone https://github.com/Prajwalsingh0/aethercrm.git
cd aethercrm

# Terminal 1 — backend (H2, no Postgres required)
cd backend && mvn spring-boot:run

# Terminal 2 — frontend
cd frontend && npm install && npm run dev
```

- App: http://localhost:5173  
- API docs: http://localhost:8080/swagger-ui.html  

## 5-minute demo script

1. **Register** a new org (name, slug, email, password).
2. **Dashboard** — metrics, smart insights, hot leads, INR pipeline.
3. **Leads** — create a lead with company, email, title `CEO`, source `REFERRAL` → high score.
4. **Convert** the lead → Contact + Account.
5. **Pipeline** — add a deal, move stages.
6. **Activities** — create a task; complete it.
7. **Support** — open a ticket; change status.
8. **Email** — compose (log-only without SMTP).
9. **AI Command** — ask “score leads”, then **confirm** an action (tasks / rescore / email).
10. **Audit** — see CREATED / SCORED / AI_ACTION events.

## Optional env

```bash
# Real email
export MAIL_ENABLED=true MAIL_HOST=... MAIL_USERNAME=... MAIL_PASSWORD=... MAIL_FROM=...

# Live AI
export AI_PROVIDER=openai OPENAI_API_KEY=sk-...
```

## Scope of v1 (complete)

| Module | Status |
|--------|--------|
| Multi-tenant auth (JWT) | Done |
| Leads + convert + explainable score | Done |
| Accounts / Contacts | Done |
| Pipeline + stages | Done |
| Products | Done |
| Activities | Done |
| Support tickets | Done |
| Campaigns | Done |
| Knowledge base | Done |
| Workflow definitions | Done |
| Email (SMTP / log-only) | Done |
| AI chat + executable actions | Done |
| Insights + audit | Done |
| Docker Compose | Done |

## Out of scope for v1 (future)

- Full workflow execution engine
- Inbound email / Gmail OAuth
- Native mobile apps
- Production SSO / SCIM
