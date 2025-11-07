package com.shop.admin.dto;

import java.math.BigDecimal;

public record OrderItemView(
        Long productId,
        String productName,
        Integer quantity,
        BigDecimal subtotal
) {
}
