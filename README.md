# SmartClass Learning System

SmartClass is a full-stack learning platform for graduation projects and portfolio demonstrations. The repository contains a Spring Boot backend, a Vue user frontend, and a React admin frontend.

## Project Structure

```text
studysys/
├── learning-system/                 # Spring Boot backend
├── smartclass-frontend-main/         # Vue 3 user frontend
├── smartclass-manage-frontend-main/  # React/Umi admin frontend
└── README.md
```

## Tech Stack

- Backend: Spring Boot 2.7.2, Java 17, MyBatis-Plus, MySQL, Redis, Elasticsearch, Knife4j, Netty WebSocket, Dify API, Tencent COS optional upload support.
- User frontend: Vue 3, TypeScript, Rsbuild, Pinia, Vue Router, Vant, Axios, Capacitor.
- Admin frontend: React 18, TypeScript, Umi Max, Ant Design Pro, ECharts.
- Deployment examples: Docker, Docker Compose, Nginx.

## Local Startup

### 1. Backend

```bash
cd learning-system
copy src\main\resources\application-example.yml src\main\resources\application.yml
```

Edit `application.yml`, then initialize MySQL:

```bash
mysql -uroot -p -e "CREATE DATABASE IF NOT EXISTS smart_class DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
mysql -uroot -p smart_class < sql/smart_class.sql
```

Start the backend:

```bash
mvn spring-boot:run
```

Backend base URL: `http://localhost:8101/api`

Knife4j docs: `http://localhost:8101/api/doc.html`

### 2. User Frontend

```bash
cd smartclass-frontend-main
copy .env.example .env
npm install
npm run dev
```

The development proxy forwards `/api` requests to `http://localhost:8101`.

### 3. Admin Frontend

```bash
cd smartclass-manage-frontend-main
copy .env.example .env
npm install
npm run start:dev
```

The admin frontend runs on `http://localhost:8000` by default and proxies `/api` to `http://localhost:8101`.

## Docker

Backend:

```bash
cd learning-system
docker compose up -d
```

User frontend:

```bash
cd smartclass-frontend-main
docker compose up -d
```

Admin frontend:

```bash
cd smartclass-manage-frontend-main
docker compose up -d
```

The frontend Docker Compose files serve static files with Nginx. Runtime API addresses should be handled by Nginx proxy configuration or build-time environment variables; plain compose environment variables are not automatically injected into already-built frontend assets.

## Public Release Notes

- Real Spring Boot configs are ignored; use `application-example.yml` as the template.
- Real frontend `.env` files are ignored; use `.env.example` as the template.
- Runtime upload files should not be committed except intentional demo resources.
- SQL initialization data uses demo accounts and placeholder keys. Replace them before real deployment.
- `smartclass-manage-frontend-main/vue-course-system` is a historical standalone Vue course-management prototype and is not used by the current React admin app.

## Default Demo Data

The SQL file contains demo users, demo AI avatars, and demo course resources for local initialization. These are suitable for development and demonstration only.
