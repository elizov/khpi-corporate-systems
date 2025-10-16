package com.shop.app.service;

import com.shop.app.model.Product;
import com.shop.app.repository.ProductJdbcRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductJdbcRepository productJdbcRepository;

    public ProductService(ProductJdbcRepository productJdbcRepository) {
        this.productJdbcRepository = productJdbcRepository;
    }

    @Transactional(readOnly = true)
    public List<Product> getProducts(BigDecimal minPrice, BigDecimal maxPrice, String search, String sort) {

        // Price validation
        if (minPrice != null && minPrice.compareTo(BigDecimal.ZERO) < 0) {
            minPrice = BigDecimal.ZERO;
        }
        if (maxPrice != null && maxPrice.compareTo(BigDecimal.ZERO) < 0) {
            maxPrice = null; // ignore invalid
        }
        if (maxPrice != null && minPrice != null && maxPrice.compareTo(minPrice) < 0) {
            // if max < min → swap
            BigDecimal tmp = minPrice;
            minPrice = maxPrice;
            maxPrice = tmp;
        }

        // Search string validation
        if (search != null) {
            search = search.trim();
            if (search.isEmpty()) {
                search = null;
            }
        }

        // Sort validation
        if (sort != null) {
            sort = sort.toLowerCase();
            if (!sort.equals("asc") && !sort.equals("desc")) {
                sort = null; // if not "asc"/"desc", then disable sorting
            }
        }

        return productJdbcRepository.findWithFilters(minPrice, maxPrice, search, sort);
    }

    @Transactional(readOnly = true)
    public Optional<Product> getProductById(Long id) {
        if (id == null || id <= 0) {
            return Optional.empty();
        }
        return productJdbcRepository.findById(id);
    }
}
