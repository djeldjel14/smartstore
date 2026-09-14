package com.smartstore.smartstore.controller;

import com.smartstore.smartstore.dto.ApiResponse;
import com.smartstore.smartstore.dto.ProductDTO;
import com.smartstore.smartstore.entity.Product;
import com.smartstore.smartstore.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
@Slf4j
public class ProductRestController {

    private final ProductService productService;

    public ProductRestController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductDTO>>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        log.debug("Fetching products - page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products = productService.getAllProducts(pageable);
        Page<ProductDTO> productDTOs = products.map(ProductDTO::new);
        return ResponseEntity.ok(ApiResponse.success(productDTOs));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDTO>> getProductById(@PathVariable Long id) {
        log.debug("Fetching product: {}", id);
        Product product = productService.getProductById(id);
        return ResponseEntity.ok(ApiResponse.success(new ProductDTO(product)));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<ProductDTO>>> searchProducts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        log.debug("Searching products: {}", keyword);
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products = productService.searchProducts(keyword, pageable);
        Page<ProductDTO> productDTOs = products.map(ProductDTO::new);
        return ResponseEntity.ok(ApiResponse.success(productDTOs));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse<Page<ProductDTO>>> getProductsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        log.debug("Fetching products for category: {}", categoryId);
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products = productService.getProductsByCategory(categoryId, pageable);
        Page<ProductDTO> productDTOs = products.map(ProductDTO::new);
        return ResponseEntity.ok(ApiResponse.success(productDTOs));
    }

    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getRecentProducts() {
        log.debug("Fetching recent products");
        List<Product> products = productService.getRecentProducts();
        List<ProductDTO> productDTOs = products.stream()
                .map(ProductDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(productDTOs));
    }
}
