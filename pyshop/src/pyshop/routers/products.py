from __future__ import annotations

from decimal import Decimal
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
    min_price: Optional[Decimal] = Query(None, ge=0),
    max_price: Optional[Decimal] = Query(None, ge=0),
    search: Optional[str] = Query(None),
    sort_field: Optional[str] = Query(None, alias="sortField"),
    sort_direction: Optional[str] = Query(None, alias="sortDirection"),
    session: Session = Depends(db_session),
):
    query = session.query(Product)

    if min_price is not None:
        query = query.filter(Product.price >= min_price)
    if max_price is not None:
        query = query.filter(Product.price <= max_price)
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


@router.get("/{product_id}", response_model=ProductResponse)
def get_product(product_id: int, session: Session = Depends(db_session)):
    product = session.get(Product, product_id)
    if not product:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Product not found")
    return product
