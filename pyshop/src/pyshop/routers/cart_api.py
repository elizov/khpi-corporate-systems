from __future__ import annotations

from decimal import Decimal
from typing import Optional

from fastapi import APIRouter, Depends, HTTPException, Request, status
from pydantic import BaseModel
from sqlalchemy.orm import Session

from ..dependencies import db_session
from ..models import Product
from ..services.cart_session import load_cart, save_cart


class AddToCartRequest(BaseModel):
    productId: int


class UpdateCartItemRequest(BaseModel):
    quantity: int


class CartActionResponse(BaseModel):
    productId: int
    quantity: Optional[int]
    subtotal: float
    totalQuantity: int
    totalPrice: float
    removed: bool
    message: str


router = APIRouter(prefix="/api/cart", tags=["Cart"])


@router.post(
    "/items",
    response_model=CartActionResponse,
    summary="Add item to cart",
    responses={404: {"description": "Product not found"}},
)
def add_to_cart(request: Request, payload: AddToCartRequest, session: Session = Depends(db_session)):
    product = session.get(Product, payload.productId)
    if not product:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Product not found")

    cart = load_cart(request)
    item = cart.add(product.id, product.name, Decimal(str(product.price)))
    save_cart(request, cart)

    return CartActionResponse(
        productId=product.id,
        quantity=item.quantity,
        subtotal=float(item.subtotal),
        totalQuantity=cart.total_quantity,
        totalPrice=float(cart.total_price),
        removed=False,
        message="Product added to cart",
    )


@router.put(
    "/items/{product_id}",
    response_model=CartActionResponse,
    summary="Update cart item quantity",
    responses={404: {"description": "Product not found in cart"}},
)
def update_cart_item(request: Request, product_id: int, payload: UpdateCartItemRequest):
    cart = load_cart(request)
    if product_id not in cart.items:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Product not found in cart")

    cart.update_quantity(product_id, payload.quantity)
    save_cart(request, cart)

    removed = payload.quantity <= 0
    item = cart.items.get(product_id)

    return CartActionResponse(
        productId=product_id,
        quantity=item.quantity if item else 0,
        subtotal=float(item.subtotal) if item else 0,
        totalQuantity=cart.total_quantity,
        totalPrice=float(cart.total_price),
        removed=removed,
        message="Quantity updated" if not removed else "Product removed from cart",
    )


@router.delete(
    "/items/{product_id}",
    response_model=CartActionResponse,
    summary="Remove item from cart",
    responses={404: {"description": "Product not found in cart"}},
)
def remove_cart_item(request: Request, product_id: int):
    cart = load_cart(request)
    if product_id not in cart.items:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Product not found in cart")
    cart.remove(product_id)
    save_cart(request, cart)

    return CartActionResponse(
        productId=product_id,
        quantity=None,
        subtotal=0,
        totalQuantity=cart.total_quantity,
        totalPrice=float(cart.total_price),
        removed=True,
        message="Product removed from cart",
    )
