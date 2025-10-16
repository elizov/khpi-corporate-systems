package com.shop.app.controller;

import com.shop.app.model.CartItem;
import com.shop.app.model.CheckoutForm;
import com.shop.app.model.Order;
import com.shop.app.service.CartService;
import com.shop.app.service.OrderService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Controller
public class CheckoutController {

    private static final String CHECKOUT_FORM_SESSION_KEY = "checkoutForm";
    private static final String PAYMENT_METHOD_CASH = "Cash on Delivery";
    private static final Pattern CARD_NUMBER_PATTERN = Pattern.compile("^[0-9]{12,19}$");

    private final CartService cartService;
    private final OrderService orderService;

    public CheckoutController(CartService cartService, OrderService orderService) {
        this.cartService = cartService;
        this.orderService = orderService;
    }

    @GetMapping("/checkout")
    public String showCheckoutForm(Model model, HttpSession session) {
        List<CartItem> items = new ArrayList<>(cartService.getItems(session));
        if (items.isEmpty()) {
            return "redirect:/cart";
        }

        CheckoutForm checkoutForm = (CheckoutForm) session.getAttribute(CHECKOUT_FORM_SESSION_KEY);
        if (checkoutForm == null) {
            checkoutForm = new CheckoutForm();
        }

        populateCartData(model, session, items);
        populateOptions(model);

        model.addAttribute("checkoutForm", checkoutForm);

        return "checkout";
    }

    @PostMapping("/checkout")
    public String processCheckout(
            @Valid @ModelAttribute("checkoutForm") CheckoutForm checkoutForm,
            BindingResult bindingResult,
            Model model,
            HttpSession session
    ) {
        List<CartItem> items = new ArrayList<>(cartService.getItems(session));
        if (items.isEmpty()) {
            return "redirect:/cart";
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
            populateCartData(model, session, items);
            populateOptions(model);
            return "checkout";
        }

        session.setAttribute(CHECKOUT_FORM_SESSION_KEY, checkoutForm);
        return "redirect:/checkout/confirm";
    }

    @GetMapping("/checkout/confirm")
    public String confirmCheckout(Model model, HttpSession session) {
        List<CartItem> items = new ArrayList<>(cartService.getItems(session));
        if (items.isEmpty()) {
            return "redirect:/cart";
        }

        CheckoutForm checkoutForm = (CheckoutForm) session.getAttribute(CHECKOUT_FORM_SESSION_KEY);
        if (checkoutForm == null) {
            return "redirect:/checkout";
        }

        populateCartData(model, session, items);
        model.addAttribute("checkoutForm", checkoutForm);
        model.addAttribute("cashPaymentMethod", PAYMENT_METHOD_CASH);
        boolean cardRequired = requiresCard(checkoutForm.getPaymentMethod());
        model.addAttribute("cardRequired", cardRequired);
        if (cardRequired) {
            model.addAttribute("maskedCardNumber", maskCardNumber(checkoutForm.getCardNumber()));
        }

        return "checkout-confirm";
    }

    @PostMapping("/checkout/confirm")
    public String finalizeCheckout(HttpSession session) {
        CheckoutForm checkoutForm = (CheckoutForm) session.getAttribute(CHECKOUT_FORM_SESSION_KEY);
        if (checkoutForm == null) {
            return "redirect:/checkout";
        }

        List<CartItem> items = new ArrayList<>(cartService.getItems(session));
        if (items.isEmpty()) {
            return "redirect:/cart";
        }

        int totalQuantity = cartService.getTotalQuantity(session);
        BigDecimal totalPrice = cartService.getTotalPrice(session);

        Order order = orderService.createOrder(checkoutForm, items, totalQuantity, totalPrice);
        cartService.clearCart(session);
        session.removeAttribute(CHECKOUT_FORM_SESSION_KEY);

        return "redirect:/order/confirmed/" + order.getId();
    }

    @GetMapping("/order/confirmed/{orderNumber}")
    public String showConfirmedOrder(@PathVariable String orderNumber, Model model) {
        Optional<Order> orderOptional = orderService.getOrderById(orderNumber);
        if (orderOptional.isEmpty()) {
            return "redirect:/products";
        }

        Order order = orderOptional.get();
        model.addAttribute("order", order);
        boolean cardRequired = requiresCard(order.getPaymentMethod());
        model.addAttribute("cardRequired", cardRequired);
        model.addAttribute("cashPaymentMethod", PAYMENT_METHOD_CASH);
        if (cardRequired) {
            model.addAttribute("maskedCardNumber", maskCardNumber(order.getCardLastFour()));
        }

        return "order-confirmed";
    }

    private void populateCartData(Model model, HttpSession session, List<CartItem> items) {
        model.addAttribute("items", items);
        model.addAttribute("totalQuantity", cartService.getTotalQuantity(session));
        model.addAttribute("totalPrice", cartService.getTotalPrice(session));
    }

    private void populateOptions(Model model) {
        model.addAttribute("paymentMethods", List.of("Credit Card", "PayPal", "Cash on Delivery"));
        model.addAttribute("deliveryMethods", List.of("Courier Delivery", "Pickup Point", "Nova Poshta"));
        model.addAttribute("cashPaymentMethod", PAYMENT_METHOD_CASH);
    }

    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.isBlank()) {
            return "N/A";
        }
        String value = cardNumber.replaceAll("\\s+", "");
        if (value.length() < 4) {
            return "****";
        }
        String lastFour = value.substring(value.length() - 4);
        return "**** **** **** " + lastFour;
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
}
