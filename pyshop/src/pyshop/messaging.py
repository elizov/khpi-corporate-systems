from __future__ import annotations

import json
from contextlib import AbstractContextManager
from typing import Any

import pika

from .config import get_settings


class RabbitPublisher(AbstractContextManager):
    def __init__(self):
        self.settings = get_settings()
        params = pika.URLParameters(self.settings.rabbitmq_url.unicode_string())
        self.connection = pika.BlockingConnection(params)
        self.channel = self.connection.channel()
        self.channel.queue_declare(queue=self.settings.queues_new, durable=True)

    def __enter__(self):
        return self

    def publish_new_order(self, payload: dict[str, Any]) -> None:
        body = json.dumps(payload).encode("utf-8")
        self.channel.basic_publish(
            exchange="",
            routing_key=self.settings.queues_new,
            body=body,
            properties=pika.BasicProperties(
                content_type="application/json",
                delivery_mode=2,
            ),
        )

    def close(self) -> None:
        if self.connection and self.connection.is_open:
            self.connection.close()

    def __exit__(self, exc_type, exc_val, exc_tb):
        self.close()
