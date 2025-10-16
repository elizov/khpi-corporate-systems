package com.shop.app.controller;

import com.shop.app.model.CartItem;
import com.shop.app.model.Product;
import com.shop.app.service.CartService;
import com.shop.app.service.ProductService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/cart")
public class CartRestController {

    private final ProductService productService;
    private final CartService cartService;

    public CartRestController(ProductService productService, CartService cartService) {
        this.productService = productService;
        this.cartService = cartService;
    }

    @PostMapping("/items")
    public ResponseEntity<?> addToCart(@RequestBody AddToCartRequest request, HttpSession session) {
        if (request == null || request.getProductId() == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Product id is required"));
        }

        Optional<Product> productOptional = productService.getProductById(request.getProductId());
        if (productOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Product not found"));
        }

        CartItem item = cartService.addProduct(productOptional.get(), session);
        int totalQuantity = cartService.getTotalQuantity(session);
        BigDecimal totalPrice = cartService.getTotalPrice(session);

        AddToCartResponse response = new AddToCartResponse(
                item.getProductId(),
                item.getQuantity(),
                totalQuantity,
                totalPrice,
                "Product added to cart"
        );
        return ResponseEntity.ok(response);
    }

    public static class AddToCartRequest {
        private Long productId;

        public Long getProductId() {
            return productId;
        }

        public void setProductId(Long productId) {
            this.productId = productId;
        }
    }

    public static class AddToCartResponse {
        private final Long productId;
        private final int quantity;
        private final int totalQuantity;
        private final BigDecimal totalPrice;
        private final String message;

        public AddToCartResponse(Long productId, int quantity, int totalQuantity, BigDecimal totalPrice, String message) {
            this.productId = productId;
            this.quantity = quantity;
            this.totalQuantity = totalQuantity;
            this.totalPrice = totalPrice;
            this.message = message;
        }

        public Long getProductId() {
            return productId;
        }

        public int getQuantity() {
            return quantity;
        }

        public int getTotalQuantity() {
            return totalQuantity;
        }

        public BigDecimal getTotalPrice() {
            return totalPrice;
        }

        public String getMessage() {
            return message;
        }
    }
}
