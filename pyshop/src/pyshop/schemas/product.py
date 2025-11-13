from __future__ import annotations

from decimal import Decimal
from typing import Optional

from pydantic import BaseModel, Field


class ProductBase(BaseModel):
    name: str = Field(..., max_length=255)
    category: str = Field(..., max_length=100)
    price: Decimal = Field(..., gt=0)
    description: Optional[str] = Field(None, max_length=500)


class ProductCreate(ProductBase):
    pass


class ProductUpdate(ProductBase):
    pass


class ProductResponse(ProductBase):
    id: int

    class Config:
        from_attributes = True


class ProductPatch(BaseModel):
    name: Optional[str] = Field(None, max_length=255)
    category: Optional[str] = Field(None, max_length=100)
    price: Optional[Decimal] = Field(None, gt=0)
    description: Optional[str] = Field(None, max_length=500)

    def has_updates(self) -> bool:
        return any(
            getattr(self, field) is not None
            for field in ("name", "category", "price", "description")
        )
