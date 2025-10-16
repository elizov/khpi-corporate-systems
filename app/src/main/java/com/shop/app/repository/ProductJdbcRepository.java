package com.shop.app.repository;

import com.shop.app.model.Product;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    public List<Product> findWithFilters(BigDecimal minPrice, BigDecimal maxPrice, String search, String sort) {
        StringBuilder sql = new StringBuilder("SELECT * FROM products WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (minPrice != null) {
            sql.append("AND price >= ? ");
            params.add(minPrice);
        }
        if (maxPrice != null) {
            sql.append("AND price <= ? ");
            params.add(maxPrice);
        }

        if (search != null && !search.trim().isEmpty()) {
            sql.append("AND (LOWER(name) LIKE ? OR LOWER(description) LIKE ?) ");
            String like = "%" + search.toLowerCase() + "%";
            params.add(like);
            params.add(like);
        }

        if ("asc".equalsIgnoreCase(sort)) {
            sql.append("ORDER BY price ASC");
        } else if ("desc".equalsIgnoreCase(sort)) {
            sql.append("ORDER BY price DESC");
        }

        return jdbcTemplate.query(sql.toString(), productRowMapper, params.toArray());
    }

    public Optional<Product> findById(Long id) {
        List<Product> products = jdbcTemplate.query(
                "SELECT * FROM products WHERE id = ?",
                productRowMapper,
                id
        );
        return products.stream().findFirst();
    }
}
