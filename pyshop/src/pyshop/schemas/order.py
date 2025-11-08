from __future__ import annotations

from decimal import Decimal
from datetime import datetime
from typing import List, Optional

from pydantic import BaseModel, Field, constr


class OrderItemPayload(BaseModel):
    product_id: Optional[int] = None
    product_name: str
    quantity: int = Field(..., gt=0)
    unit_price: Decimal = Field(..., gt=0)


class OrderCreate(BaseModel):
    full_name: str = Field(..., max_length=120)
    email: str = Field(..., max_length=150)
    phone: str = Field(..., max_length=30)
    address: str = Field(..., max_length=200)
    city: str = Field(..., max_length=100)
    postal_code: str = Field(..., max_length=20)
    delivery_method: str = Field(..., max_length=60)
    payment_method: str = Field(..., max_length=60)
    card_number: Optional[constr(pattern=r"^\d{12,19}$")] = None
    notes: Optional[str] = Field(None, max_length=300)
    items: List[OrderItemPayload]


class OrderItemResponse(BaseModel):
    product_id: Optional[int]
    product_name: str
    quantity: int
    unit_price: Decimal
    subtotal: Decimal

    class Config:
        from_attributes = True


class OrderResponse(BaseModel):
    id: str
    full_name: str
    email: str
    phone: str
    address: str
    city: str
    postal_code: str
    delivery_method: str
    payment_method: str
    card_last_four: Optional[str] = None
    notes: Optional[str] = None
    created_at: datetime
    total_quantity: int
    total_price: Decimal
    status: str
    items: List[OrderItemResponse]

    class Config:
        from_attributes = True
