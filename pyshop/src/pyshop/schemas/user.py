from __future__ import annotations

from pydantic import BaseModel, EmailStr, Field


class UserRegister(BaseModel):
    username: str = Field(..., min_length=3, max_length=50)
    email: EmailStr
    password: str = Field(..., min_length=6)
    age: int = Field(..., ge=18, le=100)


class UserLogin(BaseModel):
    username: str
    password: str


class UserSession(BaseModel):
    id: int
    username: str
