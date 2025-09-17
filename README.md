# TeamSync – Distributed Team Collaboration Platform

TeamSync is a microservices-based platform for team collaboration that provides user management, authentication, project and task management, messaging, notifications, an API gateway, an AI backend, and a modern frontend.

### Hosted Instance

- API Gateway: `http://13.60.242.32:8080`
- AI Backend: `http://13.60.242.32:8000`
- Frontend: `http://13.60.242.32:3000`

Note: Replace the IPs with your deployment addresses as needed.

## Repository Structure

- `api-gateway/` – Spring Boot gateway
- `auth-service/` – Authentication & JWT issuance
- `user-management-service/` – Users and profiles
- `project-management-service/` – Projects and membership
- `task-management-service/` – Tasks, events
- `feed-management-service/` – Feed, media storage (Azure)
- `message-management-service/` – Messaging
- `notification-service/` – Notifications (MongoDB + Kafka)
- `teamsync-ai-backend/` – Python FastAPI AI backend (LLM, RAG)
- `teamsync-social-flow/` – Frontend (Vite + React)
- `kafka-docker-compose.yml` – Zookeeper, Kafka, Schema Registry, Kafka UI

## Prerequisites

- Docker 24+
- Docker Compose
- Java 17+ and Maven (for local builds of services)
- Python 3.11+ (for AI backend local dev)

## Quick Start

### 1) Network

Create the shared network used by all services:

```bash
docker network create teamsync_network || true
```

### 2) Environment Files

Each service contains a `.env.example`. Copy and customize:

```bash
cp api-gateway/.env.example api-gateway/.env
cp auth-service/.env.example auth-service/.env
cp user-management-service/.env.example user-management-service/.env
cp project-management-service/.env.example project-management-service/.env
cp task-management-service/.env.example task-management-service/.env
cp feed-management-service/.env.example feed-management-service/.env
cp message-management-service/.env.example message-management-service/.env
cp notification-service/.env.example notification-service/.env
cp teamsync-ai-backend/.env.example teamsync-ai-backend/.env
cp teamsync-social-flow/.env.example teamsync-social-flow/.env
```

Important: Never commit your `.env`. The examples contain placeholders; set your own secrets.

### 3) Kafka Stack (optional but recommended)

```bash
docker compose -f kafka-docker-compose.yml up -d
```

### 4) Deploy Options

- Deploy using Docker Hub images:

```bash
bash scripts/deploy.sh
```

- Build and deploy locally (no Docker Hub push):

```bash
bash scripts/build_and_deploy.sh
```

Both scripts will stop existing containers with the same names and attach to the shared `teamsync_network` [[memory:5765533]]. They will also pass per-service `.env` files when present.

## Local Development

### Backend services (Java)

From any service directory (e.g., `auth-service/`):

```bash
./mvnw clean package -DskipTests
java -jar target/*.jar
```

### AI Backend (Python)

```bash
cd teamsync-ai-backend
python -m venv .venv && source .venv/bin/activate
pip install -r requirements.txt
uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
```

### Frontend (Vite + React)

```bash
cd teamsync-social-flow
npm install
npm run dev
```

## Environment Variables (Highlights)

- JWT configuration appears in several services; set `JWT_SECRET` consistently.
- Database URLs: Postgres per service, MongoDB for notifications.
- Kafka: `SPRING_KAFKA_BOOTSTRAP_SERVERS=broker:29092`, Schema Registry at `http://schema-registry:8081`.
- Azure storage for media (feed and message services): `AZURE_CONNECTION_STRING`, `AZURE_CONTAINER_NAME`, etc.
- AI backend integrates LLM providers via `GEMINI_API_KEY` and `DEEPSEEK_API_KEY`. Qdrant via `QUADRANT_URL`, `QUADRANT_API_KEY`.

Refer to each service’s `.env.example` for the full list.

## Architecture Overview

- API Gateway routes to microservices.
- Auth issues JWTs used across services.
- User, Project, Task, Feed, Message microservices with dedicated databases.
- Notifications via Kafka topics and MongoDB storage.
- AI Backend provides AI features (RAG, summarization, assistants) consumed by frontend.
- Frontend calls API Gateway and AI Backend.

### Data Stores

- Postgres: `auth`, `user`, `project`, `task`, `feed`, `message`
- MongoDB: `notification`
- Kafka + Schema Registry for events

### Networking

All containers share the external Docker network `teamsync_network` to simplify cross-service discovery.

## CI/CD Notes

- The provided scripts mirror typical CI/CD steps locally: clear conflicting containers, pull/build, and run with `.env` files.
- When deploying in your infra, ensure secrets are injected from your secret manager rather than committing them to the repo.

## Troubleshooting

- If a container fails to start due to ports in use, stop the existing process or change the host port mapping in scripts.
- Ensure `teamsync_network` exists before deploying.
- For Kafka-dependent services, bring up `kafka-docker-compose.yml` first.

## License

MIT