package com.shop.app.controller;

import com.shop.app.model.CartItem;
import com.shop.app.model.Product;
import com.shop.app.service.CartService;
import com.shop.app.service.ProductService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        return buildCartActionResponse(item.getProductId(), item, session, "Product added to cart", false);
    }

    @PutMapping("/items/{productId}")
    public ResponseEntity<?> updateQuantity(@PathVariable Long productId,
                                            @RequestBody UpdateCartItemRequest request,
                                            HttpSession session) {
        if (request == null || request.getQuantity() == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Quantity is required"));
        }

        if (!cartService.containsProduct(productId, session)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Product not found in cart"));
        }

        if (request.getQuantity() <= 0) {
            cartService.removeProduct(productId, session);
            return buildCartActionResponse(productId, null, session, "Product removed from cart", true);
        }

        CartItem updated = cartService.updateQuantity(productId, request.getQuantity(), session);
        return buildCartActionResponse(productId, updated, session, "Quantity updated", false);
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<?> removeItem(@PathVariable Long productId, HttpSession session) {
        boolean removed = cartService.removeProduct(productId, session);
        if (!removed) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Product not found in cart"));
        }
        return buildCartActionResponse(productId, null, session, "Product removed from cart", true);
    }

    private ResponseEntity<CartActionResponse> buildCartActionResponse(Long productId,
                                                                       CartItem item,
                                                                       HttpSession session,
                                                                       String message,
                                                                       boolean removed) {
        int totalQuantity = cartService.getTotalQuantity(session);
        BigDecimal totalPrice = cartService.getTotalPrice(session);
        BigDecimal subtotal = item != null ? item.getSubtotal() : BigDecimal.ZERO;
        Integer quantity = item != null ? item.getQuantity() : null;

        CartActionResponse response = new CartActionResponse(
                productId,
                quantity,
                subtotal,
                totalQuantity,
                totalPrice,
                removed,
                message
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

    public static class UpdateCartItemRequest {
        private Integer quantity;

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }
    }

    public static class CartActionResponse {
        private final Long productId;
        private final Integer quantity;
        private final BigDecimal subtotal;
        private final int totalQuantity;
        private final BigDecimal totalPrice;
        private final boolean removed;
        private final String message;

        public CartActionResponse(Long productId,
                                  Integer quantity,
                                  BigDecimal subtotal,
                                  int totalQuantity,
                                  BigDecimal totalPrice,
                                  boolean removed,
                                  String message) {
            this.productId = productId;
            this.quantity = quantity;
            this.subtotal = subtotal;
            this.totalQuantity = totalQuantity;
            this.totalPrice = totalPrice;
            this.removed = removed;
            this.message = message;
        }

        public Long getProductId() {
            return productId;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public BigDecimal getSubtotal() {
            return subtotal;
        }

        public int getTotalQuantity() {
            return totalQuantity;
        }

        public BigDecimal getTotalPrice() {
            return totalPrice;
        }

        public boolean isRemoved() {
            return removed;
        }

        public String getMessage() {
            return message;
        }
    }
}
