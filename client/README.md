# React client

A lightweight Vite + React shell that consumes the Spring REST API.

## Quick start

```bash
cd client
npm install
npm run dev
```

- Dev server proxies `/api` to `http://localhost:3000` by default via `API_TARGET` (see `vite.config.js`).
- Minimal UI pulls the catalog from `/api/products` and drives the session cart via `/api/cart`.
- Original Thymeleaf pages are stored under `legacy-templates/` as a reference while you port features to React.
