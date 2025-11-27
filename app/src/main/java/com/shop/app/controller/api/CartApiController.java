package com.shop.app.controller.api;

import com.shop.app.client.ProductClient;
import com.shop.app.client.dto.ProductDto;
import com.shop.app.model.CartItem;
import com.shop.app.service.CartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartApiController {

    private final ProductClient productClient;
    private final CartService cartService;

    public CartApiController(ProductClient productClient, CartService cartService) {
        this.productClient = productClient;
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<CartSummaryResponse> getCart(HttpSession session) {
        return ResponseEntity.ok(buildCartSummary(session));
    }

    @PostMapping("/items")
    public ResponseEntity<?> addToCart(@RequestBody AddToCartRequest request, HttpSession session) {
        if (request == null || request.getProductId() == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Product id is required"));
        }

        ProductClient.ProductLookupResult lookup = productClient.getProductById(request.getProductId());
        if (lookup.isError()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("message", "Product service is unavailable, please try again later"));
        }
        if (lookup.isNotFound() || lookup.getProduct() == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Product not found"));
        }

        ProductDto product = lookup.getProduct();
        CartItem item = cartService.addProduct(product, session);
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

    private CartSummaryResponse buildCartSummary(HttpSession session) {
        List<CartLineResponse> lines = cartService.getItems(session).stream()
                .map(item -> new CartLineResponse(
                        item.getProductId(),
                        item.getName(),
                        item.getPrice(),
                        item.getQuantity(),
                        item.getSubtotal()
                ))
                .toList();
        int totalQuantity = cartService.getTotalQuantity(session);
        BigDecimal totalPrice = cartService.getTotalPrice(session);
        return new CartSummaryResponse(lines, totalQuantity, totalPrice);
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

    public static class CartSummaryResponse {
        private final List<CartLineResponse> items;
        private final int totalQuantity;
        private final BigDecimal totalPrice;

        public CartSummaryResponse(List<CartLineResponse> items, int totalQuantity, BigDecimal totalPrice) {
            this.items = items;
            this.totalQuantity = totalQuantity;
            this.totalPrice = totalPrice;
        }

        public List<CartLineResponse> getItems() {
            return items;
        }

        public int getTotalQuantity() {
            return totalQuantity;
        }

        public BigDecimal getTotalPrice() {
            return totalPrice;
        }
    }

    public static class CartLineResponse {
        private final Long productId;
        private final String name;
        private final BigDecimal price;
        private final int quantity;
        private final BigDecimal subtotal;

        public CartLineResponse(Long productId, String name, BigDecimal price, int quantity, BigDecimal subtotal) {
            this.productId = productId;
            this.name = name;
            this.price = price;
            this.quantity = quantity;
            this.subtotal = subtotal;
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

        public BigDecimal getSubtotal() {
            return subtotal;
        }
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
