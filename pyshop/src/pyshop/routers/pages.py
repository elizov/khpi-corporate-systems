from __future__ import annotations

from decimal import Decimal, InvalidOperation
from pathlib import Path
from typing import Optional

from fastapi import APIRouter, Depends, Request
from fastapi.responses import RedirectResponse
from fastapi.templating import Jinja2Templates
from sqlalchemy.orm import Session

from ..dependencies import db_session
from ..models import Product, Order
from ..services.cart_session import load_cart, save_cart
from ..services.order_service import create_order
from ..schemas.order import OrderCreate, OrderItemPayload, OrderResponse

templates = Jinja2Templates(directory=str(Path(__file__).resolve().parent.parent / "templates"))
router = APIRouter()

PAYMENT_METHODS = ["Credit Card", "PayPal", "Cash on Delivery"]
DELIVERY_METHODS = ["Courier Delivery", "Pickup Point", "Nova Poshta"]
CASH_PAYMENT_METHOD = "Cash on Delivery"


def build_order_context(order: Order) -> dict:
    order_response = OrderResponse.model_validate(order)
    card_last_four = order_response.card_last_four
    is_cash = (order_response.payment_method or "").strip().lower() == CASH_PAYMENT_METHOD.lower()
    card_required = bool(card_last_four and not is_cash)
    masked_card = f"**** **** **** {card_last_four}" if card_last_four else ""
    created_at_formatted = order_response.created_at.strftime("%d %b %Y %H:%M")
    return {
        "page_title": "Order confirmed",
        "order": order_response,
        "card_required": card_required,
        "masked_card_number": masked_card,
        "created_at_formatted": created_at_formatted,
    }


def _parse_decimal_query(value: Optional[str]) -> tuple[Optional[Decimal], Optional[str]]:
    if value is None:
        return None, None
    cleaned = value.strip()
    if not cleaned:
        return None, None
    try:
        parsed = Decimal(cleaned)
    except InvalidOperation:
        return None, f"'{value}' is not a valid number"
    if parsed < 0:
        return None, "Price must be non-negative"
    return parsed, None


def template_context(request: Request, session: Session, extra: Optional[dict] = None):
    cart = load_cart(request)
    context = {
        "request": request,
        "cart_count": cart.total_quantity,
    }
    if extra:
        context.update(extra)
    return context


@router.get("/")
def home(request: Request, session: Session = Depends(db_session)):
    return templates.TemplateResponse("index.html", template_context(request, session, {"page_title": "Home"}))


@router.get("/products")
def products(
    request: Request,
    minPrice: Optional[str] = None,
    maxPrice: Optional[str] = None,
    search: Optional[str] = None,
    sort: Optional[str] = None,
    session: Session = Depends(db_session),
):
    query = session.query(Product)
    parsed_min, error_min = _parse_decimal_query(minPrice)
    parsed_max, error_max = _parse_decimal_query(maxPrice)
    filter_error = error_min or error_max
    if parsed_min is not None:
        query = query.filter(Product.price >= parsed_min)
    if parsed_max is not None:
        query = query.filter(Product.price <= parsed_max)
    if search:
        query = query.filter(Product.name.ilike(f"%{search}%") | Product.description.ilike(f"%{search}%"))
    if sort == "asc":
        query = query.order_by(Product.price.asc())
    elif sort == "desc":
        query = query.order_by(Product.price.desc())

    products = query.all()
    context = {
        "page_title": "Products",
        "products": products,
        "min_price": minPrice,
        "max_price": maxPrice,
        "search": search,
        "sort": sort,
        "filter_error": filter_error,
    }
    return templates.TemplateResponse("products.html", template_context(request, session, context))


@router.get("/cart")
def cart_page(request: Request, session: Session = Depends(db_session)):
    cart = load_cart(request)
    context = {
        "page_title": "Cart",
        "items": cart.as_list(),
        "total_quantity": cart.total_quantity,
        "total_price": cart.total_price,
    }
    return templates.TemplateResponse("cart.html", template_context(request, session, context))


@router.get("/checkout")
def checkout_page(request: Request, session: Session = Depends(db_session)):
    cart = load_cart(request)
    if cart.total_quantity == 0:
        return RedirectResponse("/cart", status_code=303)
    stored_form = request.session.get("checkout_form", {})
    context = {
        "page_title": "Checkout",
        "form": stored_form,
        "items": cart.as_list(),
        "total_quantity": cart.total_quantity,
        "total_price": cart.total_price,
        "payment_methods": PAYMENT_METHODS,
        "delivery_methods": DELIVERY_METHODS,
        "cash_payment_method": CASH_PAYMENT_METHOD,
    }
    return templates.TemplateResponse("checkout.html", template_context(request, session, context))


@router.post("/checkout")
async def checkout_submit(request: Request, session: Session = Depends(db_session)):
    raw_form = await request.form()
    form = {key: raw_form.get(key) for key in raw_form.keys()}
    required_fields = ["full_name", "email", "phone", "address", "city", "postal_code", "delivery_method", "payment_method"]
    errors = []
    for field in required_fields:
        if not (form.get(field) or "").strip():
            errors.append(f"{field.replace('_', ' ').title()} is required")

    cart = load_cart(request)
    if errors:
        context = {
            "page_title": "Checkout",
            "errors": errors,
            "form": form,
            "items": cart.as_list(),
            "total_quantity": cart.total_quantity,
            "total_price": cart.total_price,
            "payment_methods": PAYMENT_METHODS,
            "delivery_methods": DELIVERY_METHODS,
            "cash_payment_method": CASH_PAYMENT_METHOD,
        }
        return templates.TemplateResponse("checkout.html", template_context(request, session, context), status_code=400)

    request.session["checkout_form"] = form
    return RedirectResponse("/checkout/confirm", status_code=303)


@router.get("/checkout/confirm")
def checkout_confirm_page(request: Request, session: Session = Depends(db_session)):
    cart = load_cart(request)
    if cart.total_quantity == 0:
        return RedirectResponse("/cart", status_code=303)

    form = request.session.get("checkout_form")
    if not form:
        return RedirectResponse("/checkout", status_code=303)

    context = {
        "page_title": "Confirm order",
        "form": form,
        "cart_items": cart.as_list(),
        "cart_total": cart.total_price,
        "total_quantity": cart.total_quantity,
    }
    return templates.TemplateResponse("checkout-confirm.html", template_context(request, session, context))


@router.post("/checkout/confirm")
def checkout_confirm(request: Request, session: Session = Depends(db_session)):
    cart = load_cart(request)
    if cart.total_quantity == 0:
        return RedirectResponse("/cart", status_code=303)

    form = request.session.get("checkout_form")
    if not form:
        return RedirectResponse("/checkout", status_code=303)

    items_payload = [
        OrderItemPayload(
            product_id=item.product_id,
            product_name=item.name,
            quantity=item.quantity,
            unit_price=Decimal(str(item.price)),
        )
        for item in cart.as_list()
    ]

    order_payload = OrderCreate(
        full_name=form.get("full_name", ""),
        email=form.get("email", ""),
        phone=form.get("phone", ""),
        address=form.get("address", ""),
        city=form.get("city", ""),
        postal_code=form.get("postal_code", ""),
        delivery_method=form.get("delivery_method", ""),
        payment_method=form.get("payment_method", ""),
        card_number=(form.get("card_number") or "").replace(" ", "") or None,
        notes=form.get("notes"),
        items=items_payload,
    )

    order = create_order(session, order_payload)

    cart.clear()
    save_cart(request, cart)
    request.session.pop("checkout_form", None)

    return RedirectResponse(f"/order/{order.id}", status_code=303)


@router.get("/order/{order_id}")
def view_order(order_id: str, request: Request, session: Session = Depends(db_session)):
    order = session.get(Order, order_id)
    if not order:
        return RedirectResponse("/products", status_code=303)
    context = build_order_context(order)
    return templates.TemplateResponse("order-confirmed.html", template_context(request, session, context))
