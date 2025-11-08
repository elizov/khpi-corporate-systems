from __future__ import annotations

from dataclasses import dataclass, field
from decimal import Decimal
from typing import Dict, List


@dataclass
class CartItem:
    product_id: int
    name: str
    price: Decimal
    quantity: int = 0

    @property
    def subtotal(self) -> Decimal:
        return (self.price or Decimal("0")) * self.quantity


@dataclass
class Cart:
    items: Dict[int, CartItem] = field(default_factory=dict)

    def add(self, product_id: int, name: str, price: Decimal) -> CartItem:
        item = self.items.get(product_id)
        if not item:
            item = CartItem(product_id, name, price, 0)
            self.items[product_id] = item
        item.quantity += 1
        return item

    def update_quantity(self, product_id: int, quantity: int) -> None:
        if product_id not in self.items:
            return
        if quantity <= 0:
            self.items.pop(product_id, None)
        else:
            self.items[product_id].quantity = quantity

    def remove(self, product_id: int) -> None:
        self.items.pop(product_id, None)

    def clear(self) -> None:
        self.items.clear()

    @property
    def total_quantity(self) -> int:
        return sum(item.quantity for item in self.items.values())

    @property
    def total_price(self) -> Decimal:
        total = Decimal("0")
        for item in self.items.values():
            total += item.subtotal
        return total

    def as_list(self) -> List[CartItem]:
        return list(self.items.values())
