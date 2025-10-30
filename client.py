#!/usr/bin/env python3
"""
Simple console client for interacting with the shop product REST API.
"""

from __future__ import annotations

import json
import sys
from typing import Any, Dict, Optional

import requests

DEFAULT_BASE_URL = "http://localhost:3000"
DEFAULT_HEADERS = {"Accept": "application/json", "Content-Type": "application/json"}


def prompt(title: str, default: Optional[str] = None) -> str:
    """
    Prompt the user for input, showing the default value.
    Hitting enter with no input returns the provided default.
    """
    if default is not None:
        prompt_text = f"{title} ({default}): "
    else:
        prompt_text = f"{title}: "

    try:
        value = input(prompt_text).strip()
    except EOFError:
        print()
        return default or ""

    if not value and default is not None:
        return default
    return value


def request_json(method: str, url: str, **kwargs: Any) -> Optional[Dict[str, Any]]:
    try:
        response = requests.request(method, url, timeout=10, **kwargs)
    except requests.RequestException as exc:
        print(f"[ERROR] Network failure: {exc}")
        return None

    if response.status_code >= 400:
        print(f"[ERROR] HTTP {response.status_code}: {response.text}")
        return None

    if not response.content:
        return None

    try:
        return response.json()
    except json.JSONDecodeError:
        print("[ERROR] Response is not valid JSON.")
        return None


def format_product(product: Dict[str, Any]) -> str:
    return (
        f"ID: {product.get('id')}\n"
        f"Name: {product.get('name')}\n"
        f"Category: {product.get('category')}\n"
        f"Price: {product.get('price')}\n"
        f"Description: {product.get('description')}\n"
    )


def list_products(base_url: str) -> None:
    sort_field = prompt("Sort field [id|name|price|category]", "id")
    sort_direction = prompt("Sort direction [asc|desc]", "asc")
    params = {"sortField": sort_field, "sortDirection": sort_direction}
    data = request_json("GET", f"{base_url}/api/products", params=params)
    if data is None:
        return
    if not data:
        print("No products found.")
        return

    for product in data:
        print("-" * 40)
        print(format_product(product))


def create_product(base_url: str) -> Optional[int]:
    name = prompt("Name", "Demo Product")
    category = prompt("Category", "Testing")
    price = prompt("Price", "123.45")
    description = prompt("Description", "Created via CLI client")

    payload = {
        "name": name,
        "category": category,
        "price": float(price),
        "description": description,
    }

    data = request_json("POST", f"{base_url}/api/products", headers=DEFAULT_HEADERS, data=json.dumps(payload))
    if data is None:
        return None

    print("Product created successfully:")
    print(format_product(data))
    return data.get("id")


def get_product(base_url: str) -> None:
    product_id = prompt("Product ID", "1")
    data = request_json("GET", f"{base_url}/api/products/{product_id}")
    if data is None:
        return
    print(format_product(data))


def replace_product(base_url: str) -> None:
    product_id = prompt("Product ID", "1")
    name = prompt("New name", "Updated Product")
    category = prompt("New category", "Testing")
    price = prompt("New price", "199.99")
    description = prompt("New description", "Replaced via CLI client")

    payload = {
        "name": name,
        "category": category,
        "price": float(price),
        "description": description,
    }

    data = request_json("PUT", f"{base_url}/api/products/{product_id}", headers=DEFAULT_HEADERS, data=json.dumps(payload))
    if data is None:
        return
    print("Product replaced successfully:")
    print(format_product(data))


def patch_product(base_url: str) -> None:
    product_id = prompt("Product ID", "1")
    name = prompt("New name (leave blank to skip)", "")
    category = prompt("New category (leave blank to skip)", "")
    price = prompt("New price (leave blank to skip)", "")
    description = prompt("New description (leave blank to skip)", "")

    payload: Dict[str, Any] = {}
    if name:
        payload["name"] = name
    if category:
        payload["category"] = category
    if price:
        payload["price"] = float(price)
    if description:
        payload["description"] = description

    if not payload:
        print("No fields provided. Nothing to update.")
        return

    data = request_json("PATCH", f"{base_url}/api/products/{product_id}", headers=DEFAULT_HEADERS, data=json.dumps(payload))
    if data is None:
        return
    print("Product updated successfully:")
    print(format_product(data))


def delete_product(base_url: str) -> None:
    product_id = prompt("Product ID", "1")
    response = requests.delete(f"{base_url}/api/products/{product_id}", timeout=10)
    if response.status_code == 204:
        print("Product deleted successfully.")
    else:
        print(f"[ERROR] HTTP {response.status_code}: {response.text}")


def main() -> None:
    base_url = prompt("API base URL", DEFAULT_BASE_URL)
    options = {
        "1": ("List products", list_products),
        "2": ("Create product", create_product),
        "3": ("View product details", get_product),
        "4": ("Replace product", replace_product),
        "5": ("Partially update product", patch_product),
        "6": ("Delete product", delete_product),
        "0": ("Exit", None),
    }

    while True:
        print("\n=== Product API Console Client ===")
        for key, (label, _) in options.items():
            print(f"{key}. {label}")

        choice = input("Select an option: ").strip()
        if choice == "0":
            print("Goodbye!")
            break

        action = options.get(choice)
        if not action:
            print("Unknown option. Please try again.")
            continue

        _, handler = action
        if handler:
            handler(base_url)


if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        print("\nInterrupted by user.")
        sys.exit(0)
