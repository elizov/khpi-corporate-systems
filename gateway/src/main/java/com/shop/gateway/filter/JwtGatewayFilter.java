package com.shop.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

@Component
public class JwtGatewayFilter implements GlobalFilter, Ordered {

    private final SecretKey key;
    private final Set<String> publicPaths;
    private final AntPathMatcher matcher = new AntPathMatcher();

    public JwtGatewayFilter(@Value("${gateway.jwt.secret:changemechangemechangemechangeme}") String secret,
                            @Value("${gateway.public-paths:/api/auth/**,/api/products/**,/api/cart/**,/api/checkout/**,/actuator/health}") List<String> publicPaths) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.publicPaths = Set.copyOf(publicPaths);
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        String method = exchange.getRequest().getMethod() != null
                ? exchange.getRequest().getMethod().name()
                : "";
        if (isPublic(path, method)) {
            return chain.filter(exchange);
        }

        List<String> authHeaders = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION);
        String authHeader = (authHeaders == null || authHeaders.isEmpty()) ? null : authHeaders.get(0);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            Long userId = claims.get("uid", Long.class);
            String role = claims.get("role", String.class);
            String username = claims.getSubject();

            exchange = exchange.mutate()
                    .request(builder -> builder
                            .header("X-User-Id", userId != null ? userId.toString() : "")
                            .header("X-User-Role", role != null ? role : "")
                            .header("X-User-Name", username != null ? username : "")
                    )
                    .build();
        } catch (Exception e) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        return chain.filter(exchange);
    }

    private boolean isPublic(String path, String method) {
        // Allow product listing/details without auth, but protect modifications
        if (matcher.match("/api/products/**", path) && !"GET".equalsIgnoreCase(method)) {
            return false;
        }
        // Checkout: allow anonymous GET/POST; protect other verbs
        if (matcher.match("/api/checkout/**", path)) {
            return "GET".equalsIgnoreCase(method) || "POST".equalsIgnoreCase(method);
        }
        // Orders: /my requires auth; other GET order lookups are public, writes stay protected
        if (matcher.match("/api/orders/my", path)) {
            return false;
        }
        if (matcher.match("/api/orders/**", path)) {
            return "GET".equalsIgnoreCase(method);
        }
        return publicPaths.stream().anyMatch(pattern -> matcher.match(pattern, path));
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
