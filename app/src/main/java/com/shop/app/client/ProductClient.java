package com.shop.app.client;

import com.shop.app.client.dto.ProductDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Component
public class ProductClient {

    private static final Logger log = LoggerFactory.getLogger(ProductClient.class);

    private final RestTemplate restTemplate;

    public ProductClient(RestTemplateBuilder restTemplateBuilder,
                         @Value("${product.service.url:http://localhost:3300}") String productServiceUrl) {
        this.restTemplate = restTemplateBuilder
                .rootUri(productServiceUrl)
                .setConnectTimeout(Duration.ofSeconds(2))
                .setReadTimeout(Duration.ofSeconds(3))
                .build();
    }

    public ProductLookupResult getProductById(Long productId) {
        if (productId == null || productId <= 0) {
            return ProductLookupResult.notFound();
        }

        try {
            ResponseEntity<ProductDto> response = restTemplate.getForEntity(
                    "/api/products/{id}",
                    ProductDto.class,
                    productId
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return ProductLookupResult.found(response.getBody());
            }
            if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
                return ProductLookupResult.notFound();
            }
            return ProductLookupResult.error();
        } catch (HttpClientErrorException.NotFound e) {
            return ProductLookupResult.notFound();
        } catch (RestClientException e) {
            log.warn("Failed to fetch product {} from product service: {}", productId, e.getMessage());
            return ProductLookupResult.error();
        }
    }

    public static class ProductLookupResult {
        private final ProductDto product;
        private final boolean notFound;
        private final boolean error;

        private ProductLookupResult(ProductDto product, boolean notFound, boolean error) {
            this.product = product;
            this.notFound = notFound;
            this.error = error;
        }

        public static ProductLookupResult found(ProductDto product) {
            return new ProductLookupResult(product, false, false);
        }

        public static ProductLookupResult notFound() {
            return new ProductLookupResult(null, true, false);
        }

        public static ProductLookupResult error() {
            return new ProductLookupResult(null, false, true);
        }

        public ProductDto getProduct() {
            return product;
        }

        public boolean isNotFound() {
            return notFound;
        }

        public boolean isError() {
            return error;
        }
    }
}
