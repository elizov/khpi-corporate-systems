from __future__ import annotations

from sqlalchemy.orm import Session

from ..database import get_session
from ..models import Product

PRODUCTS = [
    ("Lenovo ThinkPad X1 Carbon", "Electronics", 38500.00, "Lightweight business laptop with Intel i7 processor"),
    ("Apple iPhone 15", "Electronics", 42999.00, "Latest generation iPhone with A17 Bionic chip"),
    ("Xiaomi Smartwatch 8 Pro", "Electronics", 5800.00, "Smartwatch with health monitoring features"),
    ("Nike Air Max Sneakers", "Fashion", 4200.00, "Comfortable sneakers with air cushion sole"),
    ("Ikea Markus Chair", "Home & Living", 4500.00, "Ergonomic office chair with adjustable height"),
]


def run() -> None:
    with get_session() as session:
        if session.query(Product).count() > 0:
            print("Products already exist, skipping seeding.")
            return
        for name, category, price, description in PRODUCTS:
            session.add(Product(name=name, category=category, price=price, description=description))
        session.commit()
        print(f"Inserted {len(PRODUCTS)} products.")


if __name__ == "__main__":
    run()
