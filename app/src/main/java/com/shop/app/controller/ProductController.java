package com.shop.app.controller;

import com.shop.app.model.Product;
import com.shop.app.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.math.BigDecimal;
import java.util.List;

@Controller
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/products")
    public String products(
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String sort,
            Model model
    ) {
        List<Product> products = productService.getProducts(minPrice, maxPrice, search, "price", sort);

        model.addAttribute("products", products);
        model.addAttribute("title", "Products");
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("search", search);
        model.addAttribute("sort", sort);

        return "products";
    }
}
