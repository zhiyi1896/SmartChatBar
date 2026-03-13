# SmartChatBar

SmartChatBar is a full-stack community project inspired by the Niuke community. It includes a Java backend, a Vue frontend, and a standalone Python AI service.

## Overview

This repository contains three main parts:

- `backend`: community backend built with Spring Boot 3 and MyBatis-Plus
- `frontend`: web UI built with Vue 3, Vite, and Element Plus
- `python-ai-service`: AI assistant service built with FastAPI and LangChain

Core features currently covered by the project include:

- user registration, login, and logout
- post creation, comments, and replies
- likes, follows, notifications, and private messages
- WebSocket-based realtime messaging
- hot ranking, search, and UV statistics
- sensitive word filtering and role-based admin actions
- RabbitMQ and Elasticsearch integration
- AI assistant page and standalone AI service

## Tech Stack

### Backend

- Java 21
- Spring Boot 3
- Spring Security
- MyBatis-Plus
- MySQL
- Redis
- RabbitMQ
- Elasticsearch

### Frontend

- Vue 3
- Vite
- Element Plus
- Pinia
- Vue Router

### AI Service

- Python 3.12
- FastAPI
- LangChain
- DeepSeek API

## Project Structure

```text
.
|-- backend
|-- frontend
|-- python-ai-service
|-- docker-compose.middleware.yml
`-- niuke.md
```

## Requirements

- JDK 21
- Maven 3.9+
- Node.js 18+
- Python 3.12+
- MySQL 8
- Redis
- RabbitMQ
- Elasticsearch 8.x

## Quick Start

### 1. Start middleware

The repository includes a compose file for middleware services:

```bash
docker compose -f docker-compose.middleware.yml up -d
```

This starts:

- MySQL
- Redis
- RabbitMQ
- Elasticsearch

### 2. Start the backend

```bash
cd backend
mvn spring-boot:run
```

### 3. Start the frontend

```bash
cd frontend
npm install
npm run dev
```

### 4. Start the AI service

```bash
cd python-ai-service
python -m venv .venv
.venv\Scripts\python -m pip install -r requirements.txt
.venv\Scripts\python -m uvicorn app.main:app --host 0.0.0.0 --port 8000
```

## Configuration

### Backend config

Main backend config: [backend/src/main/resources/application.yml](/C:/Users/17431/Desktop/niu/backend/src/main/resources/application.yml)

Replace these placeholders with values from your environment:

- `YOUR_VM_IP`
- `your_email@example.com`
- `your_mail_auth_code`
- `replace-with-your-jwt-secret-key-at-least-32-bytes`

### AI service config

Copy [python-ai-service/.env.example](/C:/Users/17431/Desktop/niu/python-ai-service/.env.example) to `python-ai-service/.env`, then fill in the real values.

Important variables:

- `JAVA_API_BASE_URL`
- `DEEPSEEK_API_KEY`
- `DEEPSEEK_BASE_URL`
- `DEEPSEEK_MODEL`

## Database Initialization

Initialization SQL: [backend/src/main/resources/schema.sql](/C:/Users/17431/Desktop/niu/backend/src/main/resources/schema.sql)

Create the database first, then run this script.

## Current Status

The repository is ready for local integration and continued development:

- backend compiles
- frontend builds
- AI service dependency setup is in place
- real runtime still requires local environment values and middleware connectivity

## Repository Cleanup

The repository has already been cleaned up to avoid committing local artifacts:

- ignored `node_modules`, `.venv`, `dist`, `__pycache__`, and similar generated files
- kept local `.env` files out of version control
- removed tracked Python bytecode cache files

## Notes

- current configuration files still contain placeholders and sample values
- do not use the current config directly for production
- if your middleware runs in a VM, use the VM IP instead of `localhost`
- the AI service requires a real `DEEPSEEK_API_KEY`

## License

No license has been added yet. If you plan to open source this project, adding `MIT` or `Apache-2.0` is a good next step.