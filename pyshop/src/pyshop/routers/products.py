from __future__ import annotations

from decimal import Decimal, InvalidOperation
from typing import Optional

from fastapi import APIRouter, Depends, Query, HTTPException, status, Body
from sqlalchemy import asc, desc
from sqlalchemy.orm import Session

from ..dependencies import db_session
from ..models import Product
from ..schemas import ProductResponse, ProductCreate, ProductUpdate, ProductPatch

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


@router.post("", response_model=ProductResponse, status_code=status.HTTP_201_CREATED)
def create_product(payload: ProductCreate, session: Session = Depends(db_session)):
    product = Product(
        name=payload.name,
        category=payload.category,
        price=payload.price,
        description=payload.description,
    )
    session.add(product)
    session.commit()
    session.refresh(product)
    return product


@router.put("/{product_id}", response_model=ProductResponse)
def replace_product(product_id: int, payload: ProductUpdate, session: Session = Depends(db_session)):
    product = session.get(Product, product_id)
    if not product:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Product not found")
    product.name = payload.name
    product.category = payload.category
    product.price = payload.price
    product.description = payload.description
    session.commit()
    session.refresh(product)
    return product


@router.patch("/{product_id}", response_model=ProductResponse)
def patch_product(
    product_id: int,
    payload: ProductPatch = Body(...),
    session: Session = Depends(db_session),
):
    product = session.get(Product, product_id)
    if not product:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Product not found")

    updates = {}
    for field in ("name", "category", "price", "description"):
        value = getattr(payload, field, None)
        if value is not None:
            updates[field] = value

    if not updates:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Request body is empty or invalid")

    for key, value in updates.items():
        setattr(product, key, value)

    session.commit()
    session.refresh(product)
    return product


@router.delete("/{product_id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_product(product_id: int, session: Session = Depends(db_session)):
    product = session.get(Product, product_id)
    if not product:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Product not found")
    session.delete(product)
    session.commit()
