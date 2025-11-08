from __future__ import annotations

from fastapi import Depends
from sqlalchemy.orm import Session

from .database import get_session


def db_session() -> Session:
    with get_session() as session:
        yield session
