# Corporate Systems


## Installation

    $ brew install openjdk@17

    $ brew install maven

## Frontend client

- New React client lives in `client` (Vite setup). Run `npm install` then `npm run dev` to start the dev server with `/api` proxying to the Spring backend on `localhost:8080`.
- Legacy Thymeleaf templates are archived in `client/legacy-templates` for reference while porting flows.

## REST API highlights

- Auth: `POST /api/auth/register`, `POST /api/auth/login`, `POST /api/auth/logout`, `GET /api/auth/me`, token at `POST /api/auth/token`.
- Catalog: `GET /api/products` (+ filters) and `GET /api/products/{id}`; admin CRUD stays under `/api/products/**`.
- Cart: `GET /api/cart` for session cart snapshot; `POST/PUT/DELETE /api/cart/items` to mutate items.
- Checkout: `GET /api/checkout/options` for dropdown data, `POST /api/checkout` to create an order from the current cart.
- Orders: `GET /api/orders/my` for the authenticated user, `GET /api/orders/{id}` for order detail (owner/guest allowed).
