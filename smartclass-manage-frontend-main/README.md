# SmartClass Admin Frontend

React admin frontend for SmartClass, built with Umi Max and Ant Design Pro.

## Tech Stack

- React 18
- TypeScript
- Umi Max
- Ant Design Pro / Ant Design
- ECharts
- OpenAPI-generated service clients

## Directory Structure

```text
smartclass-manage-frontend-main/
├── config/              # Umi routes, proxy, and app config
├── mock/                # optional Umi mock files
├── public/              # static assets served by Umi/Nginx
├── scripts/             # project scripts
├── src/
│   ├── api/             # handwritten API wrappers
│   ├── components/      # shared React components
│   ├── constants/       # constants
│   ├── locales/         # i18n resources
│   ├── pages/           # Umi route pages
│   ├── services/        # generated OpenAPI clients
│   ├── utils/           # utility functions
│   └── views/           # reusable view modules
├── tests/
├── vue-course-system/   # historical standalone Vue prototype, not used by current admin app
├── Dockerfile
└── docker-compose.yml
```

## Local Startup

```bash
copy .env.example .env
npm install
npm run start:dev
```

Default URL: `http://localhost:8000`

The dev proxy in `config/proxy.ts` forwards `/api` to `http://localhost:8101`.

## Common Commands

```bash
npm run build
npm run tsc
npm run lint
npm run openapi
```

`npm run build` runs `scripts/check-jsx-unicode-attrs.js` before building.

## Routes

Routes are defined in `config/routes.ts`. Current active admin course-management pages live under `src/pages/Admin/Course`.

The `src/pages/Course*`, `src/views/course`, and `src/components/course` modules are retained as an alternate historical course-management implementation and are not registered in the current route table.

## Docker

```bash
docker compose up -d
```

The Docker image builds static assets and serves them with Nginx. The current `nginx.conf` serves the SPA only; add an `/api` proxy block if backend calls should go through the same domain in production.

## API Configuration

- Development proxy target: `http://localhost:8101`
- Backend base path: `/api`
- Generated service clients are under `src/services/backend` and `src/services/swagger`.
- Handwritten API wrappers are under `src/api`.

## Public Release Notes

- Do not commit real `.env` files.
- `mock/` is useful for offline development but `start:dev` runs with `MOCK=none`.
- `vue-course-system/` is an independent old prototype and can be removed from a lean production repository if not needed.
- Public assets in `public/` are referenced by Umi config, manifest, or login pages and should be kept unless the branding is replaced.
