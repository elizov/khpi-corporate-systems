package com.shop.admin.client;

import com.shop.admin.dto.OrderView;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
public class OrderClient {

    private final RestTemplate restTemplate;

    public OrderClient(RestTemplateBuilder builder) {
        String orderServiceUrl = System.getenv().getOrDefault("ORDER_SERVICE_URL", "http://order:3400");
        this.restTemplate = builder.rootUri(orderServiceUrl).build();
    }

    public List<OrderView> listAll(HttpServletRequest request) {
        HttpHeaders headers = buildHeaders(request);
        ResponseEntity<List<OrderView>> response = restTemplate.exchange(
                "/api/admin/orders",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<>() {}
        );
        return response.getBody();
    }

    public OrderView confirm(String orderId, String comment, HttpServletRequest request) {
        HttpHeaders headers = buildHeaders(request);
        Map<String, String> body = comment != null && !comment.isBlank()
                ? Map.of("comment", comment)
                : Map.of();
        ResponseEntity<OrderView> response = restTemplate.exchange(
                "/api/admin/orders/{id}/confirm",
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                OrderView.class,
                orderId
        );
        return response.getBody();
    }

    public OrderView cancel(String orderId, String reason, HttpServletRequest request) {
        HttpHeaders headers = buildHeaders(request);
        Map<String, String> body = reason != null ? Map.of("reason", reason) : Map.of();
        ResponseEntity<OrderView> response = restTemplate.exchange(
                "/api/admin/orders/{id}/cancel",
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                OrderView.class,
                orderId
        );
        return response.getBody();
    }

    private HttpHeaders buildHeaders(HttpServletRequest request) {
        HttpHeaders headers = new HttpHeaders();
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && !authHeader.isBlank()) {
            headers.set(HttpHeaders.AUTHORIZATION, authHeader);
        }
        copyHeader(request, headers, "X-User-Id");
        copyHeader(request, headers, "X-User-Role");
        copyHeader(request, headers, "X-User-Name");
        return headers;
    }

    private void copyHeader(HttpServletRequest request, HttpHeaders headers, String name) {
        String value = request.getHeader(name);
        if (value != null && !value.isBlank()) {
            headers.set(name, value);
        }
    }
}
