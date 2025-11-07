package com.shop.admin.dto;

public record OrderEventDto(
        String type,
        OrderView order
) {
}
