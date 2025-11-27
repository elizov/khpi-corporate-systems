package com.shop.gateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.reactive.CorsWebFilter;

import java.util.Arrays;
import java.util.List;

@SpringBootApplication
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder,
                               @Value("${gateway.upstream.auth:http://auth:3200}") String authUpstream,
                               @Value("${gateway.upstream.product:http://product:3300}") String productUpstream,
                               @Value("${gateway.upstream.order:http://order:3400}") String orderUpstream,
                               @Value("${gateway.upstream.admin:http://admin:3500}") String adminUpstream) {
        return builder.routes()
                .route("auth-api", r -> r.path("/api/auth/**")
                        .filters(f -> f.stripPrefix(1)) // drop /api before forwarding to auth service
                        .uri(authUpstream))
                .route("product-api", r -> r.path("/api/products/**", "/products/**")
                        .uri(productUpstream))
                .route("admin-api", r -> r.path("/api/admin/**")
                        .uri(adminUpstream))
                .route("order-api", r -> r.path(
                                "/api/cart/**",
                                "/api/checkout/**",
                                "/api/orders/**",
                                "/cart/**",
                                "/checkout/**",
                                "/orders/**",
                                "/login",
                                "/register",
                                "/logout"
                        ).uri(orderUpstream))
                .build();
    }

    @Bean
    public CorsWebFilter corsWebFilter(@Value("${gateway.cors.allowed-origins:*}") String allowedOrigins) {
        List<String> origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toList();

        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(origins);
        configuration.setAllowedMethods(List.of(
                HttpMethod.GET.name(),
                HttpMethod.POST.name(),
                HttpMethod.PUT.name(),
                HttpMethod.PATCH.name(),
                HttpMethod.DELETE.name(),
                HttpMethod.OPTIONS.name()
        ));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return new CorsWebFilter(source);
    }
}
