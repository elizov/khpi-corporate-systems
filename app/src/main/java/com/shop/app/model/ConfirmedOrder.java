package com.shop.app.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class ConfirmedOrder {

    private final String orderNumber;
    private final CheckoutForm checkoutForm;
    private final List<CartItem> items;
    private final int totalQuantity;
    private final BigDecimal totalPrice;
    private final LocalDateTime createdAt;

    public ConfirmedOrder(String orderNumber,
                          CheckoutForm checkoutForm,
                          List<CartItem> items,
                          int totalQuantity,
                          BigDecimal totalPrice,
                          LocalDateTime createdAt) {
        this.orderNumber = orderNumber;
        this.checkoutForm = checkoutForm;
        this.items = items;
        this.totalQuantity = totalQuantity;
        this.totalPrice = totalPrice;
        this.createdAt = createdAt;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public CheckoutForm getCheckoutForm() {
        return checkoutForm;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public int getTotalQuantity() {
        return totalQuantity;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
