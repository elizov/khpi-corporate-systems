package com.shop.app.service;

import com.shop.app.model.Product;
import com.shop.app.repository.ProductRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public List<Product> getProducts(BigDecimal minPrice, BigDecimal maxPrice, String search, String sort) {

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

        if (sort != null) {
            sort = sort.toLowerCase();
            if (!sort.equals("asc") && !sort.equals("desc")) {
                sort = null;
            }
        }

        Specification<Product> specification = buildSpecification(minPrice, maxPrice, search);
        Sort sortSpec = resolveSort(sort);

        if (sortSpec.isUnsorted()) {
            return specification == null
                    ? productRepository.findAll()
                    : productRepository.findAll(specification);
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

    private Sort resolveSort(String sort) {
        if ("asc".equals(sort)) {
            return Sort.by(Sort.Direction.ASC, "price");
        }
        if ("desc".equals(sort)) {
            return Sort.by(Sort.Direction.DESC, "price");
        }
        return Sort.unsorted();
    }
}
