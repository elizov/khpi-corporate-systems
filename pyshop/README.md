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

| Method | Path                   | Опис                             |
|--------|------------------------|----------------------------------|
| GET    | `/api/products`        | Повертає список товарів із фільтрами |
| GET    | `/api/products/{id}`   | Деталі товару                    |
| POST   | `/api/orders`          | Створює замовлення, шле подію в RabbitMQ |
| GET    | `/api/orders/{orderId}`| Деталі замовлення                |

