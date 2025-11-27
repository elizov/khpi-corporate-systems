package com.shop.order.dto;

import com.shop.order.model.Order;
import com.shop.order.model.OrderItem;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class OrderResponse {
    private final String id;
    private final LocalDateTime createdAt;
    private final String fullName;
    private final String email;
    private final String phone;
    private final String address;
    private final String city;
    private final String postalCode;
    private final String deliveryMethod;
    private final String paymentMethod;
    private final String cardLastFour;
    private final String notes;
    private final String status;
    private final Integer totalQuantity;
    private final BigDecimal totalPrice;
    private final Long userId;
    private final List<OrderItemResponse> items;

    public OrderResponse(String id,
                         LocalDateTime createdAt,
                         String fullName,
                         String email,
                         String phone,
                         String address,
                         String city,
                         String postalCode,
                         String deliveryMethod,
                         String paymentMethod,
                         String cardLastFour,
                         String notes,
                         String status,
                         Integer totalQuantity,
                         BigDecimal totalPrice,
                         Long userId,
                         List<OrderItemResponse> items) {
        this.id = id;
        this.createdAt = createdAt;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.city = city;
        this.postalCode = postalCode;
        this.deliveryMethod = deliveryMethod;
        this.paymentMethod = paymentMethod;
        this.cardLastFour = cardLastFour;
        this.notes = notes;
        this.status = status;
        this.totalQuantity = totalQuantity;
        this.totalPrice = totalPrice;
        this.userId = userId;
        this.items = items;
    }

    public String getId() {
        return id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public String getCity() {
        return city;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getDeliveryMethod() {
        return deliveryMethod;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getCardLastFour() {
        return cardLastFour;
    }

    public String getNotes() {
        return notes;
    }

    public String getStatus() {
        return status;
    }

    public Integer getTotalQuantity() {
        return totalQuantity;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public Long getUserId() {
        return userId;
    }

    public List<OrderItemResponse> getItems() {
        return items;
    }

    public static OrderResponse from(Order order) {
        Objects.requireNonNull(order, "order must not be null");
        List<OrderItemResponse> mappedItems = order.getItems().stream()
                .map(OrderItemResponse::from)
                .toList();
        return new OrderResponse(
                order.getId(),
                order.getCreatedAt(),
                order.getFullName(),
                order.getEmail(),
                order.getPhone(),
                order.getAddress(),
                order.getCity(),
                order.getPostalCode(),
                order.getDeliveryMethod(),
                order.getPaymentMethod(),
                order.getCardLastFour(),
                order.getNotes(),
                order.getStatus() != null ? order.getStatus().name() : null,
                order.getTotalQuantity(),
                order.getTotalPrice(),
                order.getUserId(),
                mappedItems
        );
    }

    public static class OrderItemResponse {
        private final Long productId;
        private final String productName;
        private final Integer quantity;
        private final BigDecimal unitPrice;
        private final BigDecimal subtotal;

        public OrderItemResponse(Long productId,
                                 String productName,
                                 Integer quantity,
                                 BigDecimal unitPrice,
                                 BigDecimal subtotal) {
            this.productId = productId;
            this.productName = productName;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.subtotal = subtotal;
        }

        public Long getProductId() {
            return productId;
        }

        public String getProductName() {
            return productName;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public BigDecimal getUnitPrice() {
            return unitPrice;
        }

        public BigDecimal getSubtotal() {
            return subtotal;
        }

        static OrderItemResponse from(OrderItem orderItem) {
            Objects.requireNonNull(orderItem, "orderItem must not be null");
            return new OrderItemResponse(
                    orderItem.getProductId(),
                    orderItem.getProductName(),
                    orderItem.getQuantity(),
                    orderItem.getUnitPrice(),
                    orderItem.getSubtotal()
            );
        }
    }
}
