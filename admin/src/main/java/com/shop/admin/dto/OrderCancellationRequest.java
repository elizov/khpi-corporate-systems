package com.shop.admin.dto;

import jakarta.validation.constraints.NotBlank;

public record OrderCancellationRequest(
        @NotBlank(message = "Cancellation reason is required")
        String reason
) {
}
