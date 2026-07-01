# SmartClass User Frontend

Vue 3 mobile-first user frontend for SmartClass.

## Tech Stack

- Vue 3
- TypeScript
- Rsbuild
- Pinia
- Vue Router
- Vant
- Axios
- Capacitor for optional Android packaging

## Directory Structure

```text
smartclass-frontend-main/
├── src/
│   ├── api/          # handwritten API wrappers
│   ├── assets/       # bundled images
│   ├── capacitor/    # Capacitor service wrappers
│   ├── components/   # shared Vue components
│   ├── router/       # route definitions
│   ├── services/     # generated OpenAPI client
│   ├── stores/       # Pinia stores
│   ├── utils/        # utilities
│   └── views/        # pages
├── public/           # static assets
├── android/          # Capacitor Android project, optional for Web-only usage
├── rsbuild.config.ts
├── Dockerfile
└── docker-compose.yml
```

## Local Startup

```bash
copy .env.example .env
npm install
npm run dev
```

The dev server uses the proxy in `rsbuild.config.ts` and forwards `/api` to `http://localhost:8101`.

## Environment Variables

Use `.env` for local overrides. The important variable is:

```text
VITE_API_BASE_URL=
```

When empty, the frontend uses same-origin `/api`. For static deployment behind Nginx, configure Nginx to proxy `/api` to the backend.

## Build

```bash
npm run build
```

Preview:

```bash
npm run preview
```

## Android Packaging

The project contains Capacitor configuration and an Android project.

```bash
npm run cap:android
npm run cap:open:android
```

If the project is deployed only as a Web application, the `android/` directory is not required at runtime.

## Docker

```bash
docker compose up -d
```

The Docker image builds static files and serves them with Nginx. Compose environment variables are not injected into already-built static JavaScript at runtime; use `VITE_API_BASE_URL` at build time or configure Nginx proxying.

## Notes

- `public/icons/iconfont.js`, `iconfont.css`, and font files are used by the app.
- `public/logo.svg`, `default.jpg`, and `avatar-default.png` are used as runtime static assets.
- Generated OpenAPI files under `src/services` should be regenerated from backend API docs instead of edited manually.
