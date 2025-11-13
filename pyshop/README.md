# PyShop (FastAPI)

## Запуск

```bash
cd pyshop
python -m venv .venv
source .venv/bin/activate
pip install -e .

cp .env.example .env
uvicorn pyshop.main:app --reload
```

Swagger UI: <http://localhost:8000/docs>

Демо-дані можна створити командою:

```bash
python -m pyshop.scripts.seed_data
```

## Структура

```
pyshop/
 └─ src/pyshop
    ├─ main.py            # FastAPI application
    ├─ config.py          # Pydantic settings (DB, RabbitMQ, secrets)
    ├─ database.py        # SQLAlchemy engine/session helpers
    ├─ models.py          # ORM сутності (Product, Order, OrderItem)
    ├─ schemas/           # Pydantic схеми REST API
    ├─ routers/           # API та HTML маршрути
    ├─ messaging.py       # RabbitMQ publisher
    ├─ services/          # Бізнес-логіка (cart/order helpers)
    ├─ templates/         # Адаптовані під Jinja HTML-шаблони
    └─ static/            # Стилі/скрипти
```

## REST API

| Method | Path                               | Опис |
|--------|------------------------------------|------|
| GET    | `/api/products`                    | Повертає список товарів із фільтрами |
| GET    | `/api/products/{id}`               | Деталі товару |
| POST   | `/api/products`                    | Створює новий товар |
| PUT    | `/api/products/{id}`               | Повністю замінює існуючий товар |
| PATCH  | `/api/products/{id}`               | Частково оновлює товар |
| DELETE | `/api/products/{id}`               | Видаляє товар |
| POST   | `/api/orders`                      | Створює замовлення, шле подію в RabbitMQ |
| GET    | `/api/orders/{orderId}`            | Деталі замовлення |
| POST   | `/api/cart/items`                  | Додає товар у кошик сесії |
| PUT    | `/api/cart/items/{productId}`      | Змінює кількість або прибирає товар |
| DELETE | `/api/cart/items/{productId}`      | Видаляє товар з кошика |
| POST   | `/api/auth/token`                  | Видає Basic-token для інтеграцій |
