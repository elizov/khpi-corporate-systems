package com.shop.admin.messaging;

import java.math.BigDecimal;

public record OrderCancellationMessage(
        String orderId,
        String reason,
        BigDecimal lostAmount
) {
}
