from __future__ import annotations

from decimal import Decimal, InvalidOperation
from typing import Optional

from fastapi import APIRouter, Depends, Query, HTTPException, status
from sqlalchemy import asc, desc
from sqlalchemy.orm import Session

from ..dependencies import db_session
from ..models import Product
from ..schemas import ProductResponse

router = APIRouter(prefix="/api/products", tags=["products"])


@router.get("", response_model=list[ProductResponse])
def list_products(
    min_price: Optional[str] = Query(None, alias="minPrice"),
    max_price: Optional[str] = Query(None, alias="maxPrice"),
    search: Optional[str] = Query(None),
    sort_field: Optional[str] = Query(None, alias="sortField"),
    sort_direction: Optional[str] = Query(None, alias="sortDirection"),
    session: Session = Depends(db_session),
):
    query = session.query(Product)
    parsed_min = _parse_decimal(min_price)
    parsed_max = _parse_decimal(max_price)
    if parsed_min is not None:
        query = query.filter(Product.price >= parsed_min)
    if parsed_max is not None:
        query = query.filter(Product.price <= parsed_max)
    if search:
        like = f"%{search.lower()}%"
        query = query.filter(
            Product.name.ilike(like) | Product.description.ilike(like)
        )

    allowed = {"id": Product.id, "name": Product.name, "price": Product.price, "category": Product.category}
    sort_column = allowed.get((sort_field or "").lower())
    if sort_column is not None:
        direction = (sort_direction or "asc").lower()
        query = query.order_by(asc(sort_column) if direction == "asc" else desc(sort_column))

    return query.all()


def _parse_decimal(value: Optional[str]) -> Optional[Decimal]:
    if value is None:
        return None
    value = value.strip()
    if not value:
        return None
    try:
        parsed = Decimal(value)
    except InvalidOperation:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail=f"Invalid decimal value: {value}")
    if parsed < 0:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Price must be non-negative")
    return parsed


@router.get("/{product_id}", response_model=ProductResponse)
def get_product(product_id: int, session: Session = Depends(db_session)):
    product = session.get(Product, product_id)
    if not product:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Product not found")
    return product
