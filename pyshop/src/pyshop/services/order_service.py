from __future__ import annotations

from decimal import Decimal

from sqlalchemy.orm import Session

from ..messaging import RabbitPublisher
from ..models import Order, OrderItem
from ..schemas.order import OrderCreate


def create_order(session: Session, payload: OrderCreate) -> Order:
    if not payload.items:
        raise ValueError("Order must contain at least one item")

    total_qty = sum(item.quantity for item in payload.items)
    total_price = sum((item.unit_price or Decimal("0")) * item.quantity for item in payload.items)

    order = Order(
        full_name=payload.full_name,
        email=payload.email,
        phone=payload.phone,
        address=payload.address,
        city=payload.city,
        postal_code=payload.postal_code,
        delivery_method=payload.delivery_method,
        payment_method=payload.payment_method,
        total_quantity=total_qty,
        total_price=total_price,
        notes=payload.notes,
    )

    if payload.card_number:
        digits = payload.card_number.strip()
        order.card_last_four = digits[-4:]

    for item in payload.items:
        order_item = OrderItem(
            product_id=item.product_id,
            product_name=item.product_name,
            quantity=item.quantity,
            unit_price=item.unit_price,
            subtotal=item.unit_price * item.quantity,
        )
        order.items.append(order_item)

    session.add(order)
    session.commit()
    session.refresh(order)

    publish_message(order)
    return order


def publish_message(order: Order) -> None:
    payload = {
        "orderId": order.id,
        "username": order.full_name,
        "items": [
            {
                "productId": item.product_id,
                "productName": item.product_name,
                "quantity": item.quantity,
                "subtotal": float(item.subtotal),
            }
            for item in order.items
        ],
        "totalPrice": float(order.total_price),
    }
    with RabbitPublisher() as publisher:
        publisher.publish_new_order(payload)
