from __future__ import annotations

from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session

from ..dependencies import db_session
from ..models import Order
from ..schemas import OrderCreate, OrderResponse
from ..services.order_service import create_order

router = APIRouter(prefix="/api/orders", tags=["Orders"])


@router.post(
    "",
    response_model=OrderResponse,
    status_code=status.HTTP_201_CREATED,
    summary="Create order",
    description="Creates a new order from cart items or external clients.",
)
def create_order_endpoint(payload: OrderCreate, session: Session = Depends(db_session)):
    order = create_order(session, payload)
    return order


@router.get(
    "/{order_id}",
    response_model=OrderResponse,
    summary="Get order by id",
    responses={404: {"description": "Order not found"}},
)
def get_order(order_id: str, session: Session = Depends(db_session)):
    order = session.get(Order, order_id)
    if not order:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Order not found")
    return order
