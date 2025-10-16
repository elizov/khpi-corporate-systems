package com.shop.app.service;

import com.shop.app.model.CartItem;
import com.shop.app.model.Product;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CartService {

    private static final String CART_SESSION_KEY = "cart";
    private static final String CART_TOTAL_QUANTITY_KEY = "cartTotalQuantity";
    private static final String CART_TOTAL_PRICE_KEY = "cartTotalPrice";

    @SuppressWarnings("unchecked")
    private Map<Long, CartItem> getCart(HttpSession session) {
        Map<Long, CartItem> cart = (Map<Long, CartItem>) session.getAttribute(CART_SESSION_KEY);
        if (cart == null) {
            cart = new LinkedHashMap<>();
            session.setAttribute(CART_SESSION_KEY, cart);
        }
        return cart;
    }

    public CartItem addProduct(Product product, HttpSession session) {
        Map<Long, CartItem> cart = getCart(session);
        CartItem cartItem = cart.get(product.getId());
        if (cartItem == null) {
            cartItem = new CartItem(product.getId(), product.getName(), product.getPrice());
            cart.put(product.getId(), cartItem);
        }
        cartItem.incrementQuantity();
        updateSummaryAttributes(session, cart.values());
        return cartItem;
    }

    public List<CartItem> getItems(HttpSession session) {
        return new ArrayList<>(getCart(session).values());
    }

    public int getTotalQuantity(HttpSession session) {
        Map<Long, CartItem> cart = getCart(session);
        int totalQuantity = cart.values().stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
        session.setAttribute(CART_TOTAL_QUANTITY_KEY, totalQuantity);
        return totalQuantity;
    }

    public BigDecimal getTotalPrice(HttpSession session) {
        Map<Long, CartItem> cart = getCart(session);
        BigDecimal totalPrice = cart.values().stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        session.setAttribute(CART_TOTAL_PRICE_KEY, totalPrice);
        return totalPrice;
    }

    private void updateSummaryAttributes(HttpSession session, Collection<CartItem> items) {
        int totalQuantity = items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
        BigDecimal totalPrice = items.stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        session.setAttribute(CART_TOTAL_QUANTITY_KEY, totalQuantity);
        session.setAttribute(CART_TOTAL_PRICE_KEY, totalPrice);
    }
}
