from __future__ import annotations

import hashlib

from sqlalchemy.orm import Session

from ..models import User
from ..schemas import UserRegister, UserLogin, UserSession


def hash_password(password: str) -> str:
    return hashlib.sha256(password.encode("utf-8")).hexdigest()


def register_user(session: Session, payload: UserRegister) -> User:
    if session.query(User).filter_by(username=payload.username).first():
        raise ValueError("User with this username already exists")
    if session.query(User).filter_by(email=payload.email).first():
        raise ValueError("User with this email already exists")

    user = User(
        username=payload.username,
        email=payload.email,
        password=hash_password(payload.password),
        role="USER",
        age=payload.age,
    )
    session.add(user)
    session.commit()
    session.refresh(user)
    return user


def authenticate(session: Session, payload: UserLogin) -> UserSession | None:
    user = session.query(User).filter_by(username=payload.username).first()
    if not user:
        return None
    if user.password != hash_password(payload.password):
        return None
    return UserSession(id=user.id, username=user.username)
