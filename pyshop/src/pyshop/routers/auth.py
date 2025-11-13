from __future__ import annotations

import base64
from pathlib import Path

from fastapi import APIRouter, Depends, Request, status, Body
from fastapi.responses import RedirectResponse, JSONResponse
from fastapi.templating import Jinja2Templates
from sqlalchemy.orm import Session

from ..dependencies import db_session
from ..schemas import UserRegister, UserLogin
from ..services.user_service import register_user, authenticate
from ..services.cart_session import load_cart


templates = Jinja2Templates(directory=str(Path(__file__).resolve().parent.parent / "templates"))
router = APIRouter()


@router.get("/login")
def login_page(request: Request, session: Session = Depends(db_session)):
    return templates.TemplateResponse("login.html", _context(request, session, {"page_title": "Login"}))


@router.post("/login")
async def login_submit(request: Request, session: Session = Depends(db_session)):
    form = await request.form()
    payload = UserLogin(username=form.get("username", ""), password=form.get("password", ""))
    user_session = authenticate(session, payload)
    if not user_session:
        context = _context(request, session, {"page_title": "Login", "error": "Invalid username or password"})
        return templates.TemplateResponse("login.html", context, status_code=400)
    request.session["user"] = user_session.model_dump()
    return RedirectResponse("/", status_code=status.HTTP_303_SEE_OTHER)


@router.get("/register")
def register_page(request: Request, session: Session = Depends(db_session)):
    return templates.TemplateResponse("register.html", _context(request, session, {"page_title": "Register"}))


@router.post("/register")
async def register_submit(request: Request, session: Session = Depends(db_session)):
    form = await request.form()
    try:
        payload = UserRegister(
            username=form.get("username", ""),
            email=form.get("email", ""),
            password=form.get("password", ""),
            age=int(form.get("age", 0) or 0),
        )
    except Exception:
        context = _context(request, session, {"page_title": "Register", "form": dict(form), "error": "Please fill in all fields correctly"})
        return templates.TemplateResponse("register.html", context, status_code=400)

    try:
        register_user(session, payload)
    except ValueError as exc:
        context = _context(request, session, {"page_title": "Register", "form": dict(form), "error": str(exc)})
        return templates.TemplateResponse("register.html", context, status_code=400)

    return RedirectResponse("/login", status_code=status.HTTP_303_SEE_OTHER)


@router.get("/logout")
def logout(request: Request):
    request.session.pop("user", None)
    return RedirectResponse("/login", status_code=status.HTTP_303_SEE_OTHER)


@router.post("/api/auth/token")
def api_token(request_data: UserLogin = Body(...), session: Session = Depends(db_session)):
    user_session = authenticate(session, request_data)
    if not user_session:
        return JSONResponse(status_code=status.HTTP_401_UNAUTHORIZED, content={})

    raw_credentials = f"{request_data.username}:{request_data.password}"
    token = "Basic " + base64.b64encode(raw_credentials.encode("utf-8")).decode("utf-8")
    return {"token": token, "role": "USER"}


def _context(request: Request, session: Session, extra: dict | None = None) -> dict:
    cart = load_cart(request)
    context = {
        "request": request,
        "cart_count": cart.total_quantity,
        "current_user": request.session.get("user"),
    }
    if extra:
        context.update(extra)
    return context
