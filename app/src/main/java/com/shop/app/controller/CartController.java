package com.shop.app.controller;

import com.shop.app.model.CartItem;
import com.shop.app.service.CartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public CartSummary cart(HttpSession session) {
        List<CartItem> items = cartService.getItems(session);
        int totalQuantity = cartService.getTotalQuantity(session);
        BigDecimal totalPrice = cartService.getTotalPrice(session);
        return new CartSummary(items, totalQuantity, totalPrice);
    }

    public record CartSummary(List<CartItem> items, int totalQuantity, BigDecimal totalPrice) {
    }
}
