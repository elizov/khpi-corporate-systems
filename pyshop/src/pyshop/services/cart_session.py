from __future__ import annotations

from decimal import Decimal
from typing import Any, Dict

from fastapi import Request

from .cart import Cart, CartItem


SESSION_KEY = "cart"


def load_cart(request: Request) -> Cart:
    data = request.session.get(SESSION_KEY, {})
    cart = Cart()
    for product_id, payload in data.items():
        cart.items[int(product_id)] = CartItem(
            product_id=int(product_id),
            name=payload["name"],
            price=Decimal(str(payload["price"])),
            quantity=payload["quantity"],
        )
    return cart


def save_cart(request: Request, cart: Cart) -> None:
    serialized: Dict[str, Dict[str, Any]] = {}
    for item in cart.items.values():
        serialized[str(item.product_id)] = {
            "name": item.name,
            "price": float(item.price),
            "quantity": item.quantity,
        }
    request.session[SESSION_KEY] = serialized
