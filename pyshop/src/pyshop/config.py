from __future__ import annotations

from functools import lru_cache
from pydantic import AnyUrl, Field
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_file=".env", env_file_encoding="utf-8", extra="ignore")

    app_name: str = "PyShop"
    secret_key: str = Field("dev-secret-change-me", env="SECRET_KEY")

    database_url: AnyUrl = Field("mysql://user:pass@localhost:3306/app", env="DATABASE_URL")
    rabbitmq_url: AnyUrl = Field("amqp://user:pass@localhost:5672/", env="RABBITMQ_URL")

    queues_new: str = Field("orders.new", alias="APP_MESSAGING_QUEUES_NEW")
    queues_confirmed: str = Field("orders.confirmed", alias="APP_MESSAGING_QUEUES_CONFIRMED")
    queues_canceled: str = Field("orders.canceled", alias="APP_MESSAGING_QUEUES_CANCELED")


@lru_cache()
def get_settings() -> Settings:
    return Settings()
