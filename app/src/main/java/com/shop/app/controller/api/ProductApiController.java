package com.shop.app.controller.api;

import com.shop.app.model.Product;
import com.shop.app.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/products")
@Validated
@Tag(name = "Products", description = "Operations for managing catalog products")
public class ProductApiController {

    private final ProductService productService;

    public ProductApiController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    @Operation(
            summary = "Get all products",
            description = "Returns all products with optional filters for price range and keyword search.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of products returned")
            }
    )
    public ResponseEntity<List<ProductResponse>> getProducts(
            @Parameter(description = "Lower price bound (inclusive)") @RequestParam(required = false) BigDecimal minPrice,
            @Parameter(description = "Upper price bound (inclusive)") @RequestParam(required = false) BigDecimal maxPrice,
            @Parameter(description = "Search query applied to name or description") @RequestParam(required = false) String search,
            @Parameter(description = "Price sorting direction: asc or desc") @RequestParam(required = false) String sort
    ) {
        List<Product> products = productService.getProducts(minPrice, maxPrice, search, sort);
        return ResponseEntity.ok(products.stream().map(ProductResponse::fromEntity).toList());
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get product by id",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Product found"),
                    @ApiResponse(responseCode = "404", description = "Product not found")
            }
    )
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        return productService.getProductById(id)
                .map(ProductResponse::fromEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping
    @Operation(
            summary = "Create product",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Product created", content = @Content(schema = @Schema(implementation = ProductResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Validation failed")
            }
    )
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        Product created = productService.createProduct(request.toProduct());
        return ResponseEntity
                .created(URI.create("/api/products/" + created.getId()))
                .body(ProductResponse.fromEntity(created));
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Replace product",
            description = "Replaces the existing product with new attributes.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Product replaced"),
                    @ApiResponse(responseCode = "400", description = "Validation failed"),
                    @ApiResponse(responseCode = "404", description = "Product not found")
            }
    )
    public ResponseEntity<ProductResponse> replaceProduct(@PathVariable Long id,
                                                          @Valid @RequestBody ProductRequest request) {
        Product source = request.toProduct();
        return productService.updateProduct(id, source)
                .map(ProductResponse::fromEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PatchMapping("/{id}")
    @Operation(
            summary = "Partially update product",
            description = "Applies partial modifications to selected product fields.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Product updated"),
                    @ApiResponse(responseCode = "400", description = "Request body is empty or invalid"),
                    @ApiResponse(responseCode = "404", description = "Product not found")
            }
    )
    public ResponseEntity<ProductResponse> patchProduct(@PathVariable Long id,
                                                        @Valid @RequestBody ProductPatchRequest request) {
        if (!request.hasUpdates()) {
            return ResponseEntity.badRequest().build();
        }

        return productService.applyPartialUpdate(id, product -> {
                    if (request.getName() != null) {
                        product.setName(request.getName());
                    }
                    if (request.getCategory() != null) {
                        product.setCategory(request.getCategory());
                    }
                    if (request.getPrice() != null) {
                        product.setPrice(request.getPrice());
                    }
                    if (request.getDescription() != null) {
                        product.setDescription(request.getDescription());
                    }
                })
                .map(ProductResponse::fromEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete product",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Product deleted"),
                    @ApiResponse(responseCode = "404", description = "Product not found")
            }
    )
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        boolean deleted = productService.deleteProduct(id);
        if (!deleted) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.noContent().build();
    }

    @Schema(name = "ProductRequest", description = "Payload for creating or replacing a product")
    public static class ProductRequest {

        @NotBlank
        @Size(max = 255)
        private String name;

        @NotBlank
        @Size(max = 100)
        private String category;

        @NotNull
        @DecimalMin(value = "0.0", inclusive = false)
        private BigDecimal price;

        @Size(max = 500)
        private String description;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        private Product toProduct() {
            Product product = new Product();
            product.setName(name);
            product.setCategory(category);
            product.setPrice(price);
            product.setDescription(description);
            return product;
        }
    }

    @Schema(name = "ProductPatchRequest", description = "Payload for partially updating a product; supply only the fields to change")
    public static class ProductPatchRequest {
        @Size(max = 255)
        private String name;

        @Size(max = 100)
        private String category;

        @DecimalMin(value = "0.0", inclusive = false)
        private BigDecimal price;

        @Size(max = 500)
        private String description;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public boolean hasUpdates() {
            return name != null || category != null || price != null || description != null;
        }
    }

    @Schema(name = "ProductResponse", description = "Product representation returned by the API")
    public static class ProductResponse {
        private final Long id;
        private final String name;
        private final String category;
        private final BigDecimal price;
        private final String description;

        public ProductResponse(Long id, String name, String category, BigDecimal price, String description) {
            this.id = id;
            this.name = name;
            this.category = category;
            this.price = price;
            this.description = description;
        }

        public Long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getCategory() {
            return category;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public String getDescription() {
            return description;
        }

        static ProductResponse fromEntity(Product product) {
            Objects.requireNonNull(product, "product must not be null");
            return new ProductResponse(
                    product.getId(),
                    product.getName(),
                    product.getCategory(),
                    product.getPrice(),
                    product.getDescription()
            );
        }
    }
}
