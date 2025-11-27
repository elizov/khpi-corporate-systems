package com.shop.order.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class CartItem {
    private final Long productId;
    private final String name;
    private final BigDecimal price;
    private int quantity;

    public CartItem(Long productId, String name, BigDecimal price) {
        this.productId = productId;
        this.name = name;
        this.price = price != null ? price : BigDecimal.ZERO;
        this.quantity = 0;
    }

    public Long getProductId() {
        return productId;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void incrementQuantity() {
        this.quantity++;
    }

    public void setQuantity(int quantity) {
        this.quantity = Math.max(quantity, 0);
    }

    public BigDecimal getSubtotal() {
        return price.multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP);
    }
}
