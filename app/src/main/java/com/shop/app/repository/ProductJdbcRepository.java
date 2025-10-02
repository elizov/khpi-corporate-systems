package com.shop.app.repository;

import com.shop.app.model.Product;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public class ProductJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    public ProductJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Product> productRowMapper = (rs, rowNum) -> {
        Product product = new Product();
        product.setId(rs.getLong("id"));
        product.setName(rs.getString("name"));
        product.setCategory(rs.getString("category"));
        product.setPrice(rs.getBigDecimal("price"));
        product.setDescription(rs.getString("description"));
        return product;
    };

    public List<Product> findAll() {
        return jdbcTemplate.query("SELECT * FROM products", productRowMapper);
    }

    public List<Product> filterByPrice(BigDecimal minPrice, BigDecimal maxPrice) {
        return jdbcTemplate.query(
                "SELECT * FROM products WHERE price BETWEEN ? AND ?",
                productRowMapper,
                minPrice, maxPrice
        );
    }
}
