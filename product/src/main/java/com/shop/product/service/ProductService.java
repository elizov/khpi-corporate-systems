package com.shop.product.service;

import com.shop.product.model.Product;
import com.shop.product.repository.ProductRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public List<Product> getProducts(BigDecimal minPrice,
                                     BigDecimal maxPrice,
                                     String search,
                                     String sortField,
                                     String sortDirection) {

        if (minPrice != null && minPrice.compareTo(BigDecimal.ZERO) < 0) {
            minPrice = BigDecimal.ZERO;
        }
        if (maxPrice != null && maxPrice.compareTo(BigDecimal.ZERO) < 0) {
            maxPrice = null;
        }
        if (maxPrice != null && minPrice != null && maxPrice.compareTo(minPrice) < 0) {
            BigDecimal tmp = minPrice;
            minPrice = maxPrice;
            maxPrice = tmp;
        }

        if (search != null) {
            search = search.trim();
            if (search.isEmpty()) {
                search = null;
            }
        }

        Specification<Product> specification = buildSpecification(minPrice, maxPrice, search);
        Sort sortSpec = resolveSort(sortField, sortDirection);

        if (sortSpec.isUnsorted()) {
            return specification == null
                    ? productRepository.findAll()
                    : productRepository.findAll(specification);
        }

        if (specification == null) {
            return productRepository.findAll(sortSpec);
        }
        return productRepository.findAll(specification, sortSpec);
    }

    @Transactional(readOnly = true)
    public Optional<Product> getProductById(Long id) {
        if (id == null || id <= 0) {
            return Optional.empty();
        }
        return productRepository.findById(id);
    }

    @Transactional
    public Product createProduct(Product product) {
        return productRepository.save(product);
    }

    @Transactional
    public Optional<Product> updateProduct(Long id, Product source) {
        return productRepository.findById(id).map(existing -> {
            existing.setName(source.getName());
            existing.setCategory(source.getCategory());
            existing.setPrice(source.getPrice());
            existing.setDescription(source.getDescription());
            existing.setImageUrl(source.getImageUrl());
            return productRepository.save(existing);
        });
    }

    @Transactional
    public Optional<Product> applyPartialUpdate(Long id, Consumer<Product> updater) {
        return productRepository.findById(id).map(existing -> {
            updater.accept(existing);
            return productRepository.save(existing);
        });
    }

    @Transactional
    public boolean deleteProduct(Long id) {
        if (id == null || id <= 0 || !productRepository.existsById(id)) {
            return false;
        }
        productRepository.deleteById(id);
        return true;
    }

    private Specification<Product> buildSpecification(BigDecimal minPrice, BigDecimal maxPrice, String search) {
        Specification<Product> spec = null;

        if (minPrice != null) {
            BigDecimal min = minPrice;
            Specification<Product> minSpec = (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("price"), min);
            spec = spec == null ? minSpec : spec.and(minSpec);
        }

        if (maxPrice != null) {
            BigDecimal max = maxPrice;
            Specification<Product> maxSpec = (root, query, cb) -> cb.lessThanOrEqualTo(root.get("price"), max);
            spec = spec == null ? maxSpec : spec.and(maxSpec);
        }

        if (search != null) {
            String like = "%" + search.toLowerCase() + "%";
            Specification<Product> searchSpec = (root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("name")), like),
                    cb.like(cb.lower(root.get("description")), like)
            );
            spec = spec == null ? searchSpec : spec.and(searchSpec);
        }

        return spec;
    }

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("id", "name", "price", "category");

    private Sort resolveSort(String sortField, String sortDirection) {
        String normalizedField = normalizeSortField(sortField);
        if (normalizedField == null) {
            return Sort.unsorted();
        }

        Sort.Direction direction = normalizeSortDirection(sortDirection);
        if (direction == null) {
            direction = Sort.Direction.ASC;
        }
        return Sort.by(direction, normalizedField);
    }

    private String normalizeSortField(String sortField) {
        if (sortField == null) {
            return null;
        }
        String normalized = sortField.trim();
        if (normalized.isEmpty()) {
            return null;
        }
        normalized = normalized.toLowerCase(Locale.ROOT);
        if (!ALLOWED_SORT_FIELDS.contains(normalized)) {
            return null;
        }
        return normalized;
    }

    private Sort.Direction normalizeSortDirection(String sortDirection) {
        if (sortDirection == null) {
            return null;
        }
        String normalized = sortDirection.trim();
        if (normalized.isEmpty()) {
            return null;
        }
        normalized = normalized.toLowerCase(Locale.ROOT);
        if ("asc".equals(normalized)) {
            return Sort.Direction.ASC;
        }
        if ("desc".equals(normalized)) {
            return Sort.Direction.DESC;
        }
        return null;
    }
}
