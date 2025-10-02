package com.shop.app.service;

import com.shop.app.model.Product;
import com.shop.app.repository.ProductJdbcRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductService {

    private final ProductJdbcRepository productJdbcRepository;

    public ProductService(ProductJdbcRepository productJdbcRepository) {
        this.productJdbcRepository = productJdbcRepository;
    }

    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        return productJdbcRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Product> filterProducts(BigDecimal minPrice, BigDecimal maxPrice) {
        if (minPrice == null || minPrice.compareTo(BigDecimal.ZERO) < 0) {
            minPrice = BigDecimal.ZERO;
        }
        if (maxPrice == null || maxPrice.compareTo(minPrice) < 0) {
            maxPrice = new BigDecimal("999999999.99");
        }
        return productJdbcRepository.filterByPrice(minPrice, maxPrice);
    }
}
