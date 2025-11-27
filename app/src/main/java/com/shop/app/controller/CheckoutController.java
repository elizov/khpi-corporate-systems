package com.shop.app.controller;

import com.shop.app.dto.OrderResponse;
import com.shop.app.model.CartItem;
import com.shop.app.model.CheckoutForm;
import com.shop.app.model.Order;
import com.shop.app.model.User;
import com.shop.app.service.CartService;
import com.shop.app.service.OrderService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/checkout")
public class CheckoutController {

    private static final String PAYMENT_METHOD_CASH = "Cash on Delivery";
    private static final Pattern CARD_NUMBER_PATTERN = Pattern.compile("^[0-9]{12,19}$");

    private final CartService cartService;
    private final OrderService orderService;

    public CheckoutController(CartService cartService, OrderService orderService) {
        this.cartService = cartService;
        this.orderService = orderService;
    }

    @GetMapping("/options")
    public Map<String, Object> options() {
        return Map.of(
                "paymentMethods", List.of("Credit Card", "PayPal", "Cash on Delivery"),
                "deliveryMethods", List.of("Courier Delivery", "Pickup Point", "Nova Poshta"),
                "cashPaymentMethod", PAYMENT_METHOD_CASH
        );
    }

    @PostMapping
    public ResponseEntity<?> finalizeCheckout(@Valid @RequestBody CheckoutForm checkoutForm,
                                              BindingResult bindingResult,
                                              HttpSession session) {
        List<CartItem> items = new ArrayList<>(cartService.getItems(session));
        if (items.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Cart is empty"));
        }

        checkoutForm.setPaymentMethod(trimToNull(checkoutForm.getPaymentMethod()));
        checkoutForm.setCardNumber(trimToNull(checkoutForm.getCardNumber()));

        if (requiresCard(checkoutForm.getPaymentMethod())) {
            String sanitizedCard = sanitizeCardNumber(checkoutForm.getCardNumber());
            if (sanitizedCard == null || !CARD_NUMBER_PATTERN.matcher(sanitizedCard).matches()) {
                bindingResult.rejectValue("cardNumber", "checkout.cardNumber", "Card number must contain 12-19 digits");
            } else {
                checkoutForm.setCardNumber(sanitizedCard);
            }
        } else {
            checkoutForm.setCardNumber(null);
        }

        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message", "Validation failed",
                            "errors", collectErrors(bindingResult)
                    ));
        }

        int totalQuantity = cartService.getTotalQuantity(session);
        BigDecimal totalPrice = cartService.getTotalPrice(session);

        User currentUser = (User) session.getAttribute("user");
        Order order = orderService.createOrder(checkoutForm, items, totalQuantity, totalPrice, currentUser);
        cartService.clearCart(session);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(OrderResponse.from(order));
    }

    private boolean requiresCard(String paymentMethod) {
        return paymentMethod != null && !PAYMENT_METHOD_CASH.equalsIgnoreCase(paymentMethod.trim());
    }

    private String sanitizeCardNumber(String cardNumber) {
        if (cardNumber == null) {
            return null;
        }
        String digitsOnly = cardNumber.replaceAll("\\s+", "");
        return digitsOnly.isEmpty() ? null : digitsOnly;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private Map<String, String> collectErrors(BindingResult bindingResult) {
        Map<String, String> errors = new HashMap<>();
        bindingResult.getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        return errors;
    }
}
