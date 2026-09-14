package com.smartstore.smartstore.controller;

import com.smartstore.smartstore.dto.*;
import com.smartstore.smartstore.entity.Category;
import com.smartstore.smartstore.entity.Order;
import com.smartstore.smartstore.entity.Product;
import com.smartstore.smartstore.entity.User;
import com.smartstore.smartstore.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@Slf4j
public class AdminRestController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final OrderService orderService;
    private final UserService userService;
    private final CartService cartService;

    public AdminRestController(ProductService productService, CategoryService categoryService,
                               OrderService orderService, UserService userService, CartService cartService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.orderService = orderService;
        this.userService = userService;
        this.cartService = cartService;
    }

    // ============== PRODUCT MANAGEMENT ==============

    @PostMapping("/products")
    public ResponseEntity<ApiResponse<ProductDTO>> createProduct(@RequestBody CreateProductRequest request) {
        try {
            log.info("Admin creating new product: {}", request.getName());
            Category category = categoryService.getCategoryById(request.getCategoryId());
            
            Product product = new Product();
            product.setName(request.getName());
            product.setDescription(request.getDescription());
            product.setPrice(request.getPrice());
            product.setStock(request.getStock());
            product.setCategory(category);
            product.setImageUrl(request.getImageUrl());
            product.setAvailable(true);
            
            Product savedProduct = productService.saveProduct(product);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Product created", new ProductDTO(savedProduct)));
        } catch (Exception e) {
            log.error("Error creating product: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error creating product"));
        }
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<ApiResponse<ProductDTO>> updateProduct(
            @PathVariable Long id,
            @RequestBody CreateProductRequest request) {
        try {
            log.info("Admin updating product: {}", id);
            Product product = productService.getProductById(id);
            Category category = categoryService.getCategoryById(request.getCategoryId());
            
            product.setName(request.getName());
            product.setDescription(request.getDescription());
            product.setPrice(request.getPrice());
            product.setStock(request.getStock());
            product.setCategory(category);
            product.setImageUrl(request.getImageUrl());
            
            Product updatedProduct = productService.saveProduct(product);
            return ResponseEntity.ok(ApiResponse.success("Product updated", new ProductDTO(updatedProduct)));
        } catch (Exception e) {
            log.error("Error updating product: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error updating product"));
        }
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<ApiResponse<String>> deleteProduct(@PathVariable Long id) {
        try {
            log.info("Admin deleting product: {}", id);
            productService.deleteProduct(id);
            return ResponseEntity.ok(ApiResponse.success("Product deleted", ""));
        } catch (Exception e) {
            log.error("Error deleting product: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error deleting product"));
        }
    }

    // ============== CATEGORY MANAGEMENT ==============

    @PostMapping("/categories")
    public ResponseEntity<ApiResponse<CategoryDTO>> createCategory(@RequestBody Category category) {
        try {
            log.info("Admin creating category: {}", category.getName());
            Category savedCategory = categoryService.createCategory(category);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Category created", new CategoryDTO(savedCategory)));
        } catch (Exception e) {
            log.error("Error creating category: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error creating category"));
        }
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<ApiResponse<CategoryDTO>> updateCategory(
            @PathVariable Long id,
            @RequestBody Category categoryData) {
        try {
            log.info("Admin updating category: {}", id);
            Category updatedCategory = categoryService.updateCategory(id, categoryData);
            return ResponseEntity.ok(ApiResponse.success("Category updated", new CategoryDTO(updatedCategory)));
        } catch (Exception e) {
            log.error("Error updating category: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error updating category"));
        }
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<ApiResponse<String>> deleteCategory(@PathVariable Long id) {
        try {
            log.info("Admin deleting category: {}", id);
            categoryService.deleteCategory(id);
            return ResponseEntity.ok(ApiResponse.success("Category deleted", ""));
        } catch (Exception e) {
            log.error("Error deleting category: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error deleting category"));
        }
    }

    // ============== ORDER MANAGEMENT ==============

    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<List<OrderDTO>>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            log.debug("Admin fetching all orders - page: {}, size: {}", page, size);
            Pageable pageable = PageRequest.of(page, size);
            Page<Order> orders = orderService.getAllOrders(pageable);
            
            List<OrderDTO> orderDTOs = orders.getContent().stream()
                    .map(OrderDTO::new)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(ApiResponse.success(orderDTOs));
        } catch (Exception e) {
            log.error("Error fetching orders: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error fetching orders"));
        }
    }

    @GetMapping("/orders/status/{status}")
    public ResponseEntity<ApiResponse<List<OrderDTO>>> getOrdersByStatus(
            @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            log.debug("Admin fetching orders with status: {}", status);
            Pageable pageable = PageRequest.of(page, size);
            Page<Order> orders = orderService.getOrdersByStatus(status, pageable);
            
            List<OrderDTO> orderDTOs = orders.getContent().stream()
                    .map(OrderDTO::new)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(ApiResponse.success(orderDTOs));
        } catch (Exception e) {
            log.error("Error fetching orders by status: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error fetching orders"));
        }
    }

    // ============== USER MANAGEMENT ==============

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            log.debug("Admin fetching all users - page: {}, size: {}", page, size);
            Pageable pageable = PageRequest.of(page, size);
            Page<User> users = userService.getAllUsers(pageable);
            
            List<UserDTO> userDTOs = users.getContent().stream()
                    .map(UserDTO::new)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(ApiResponse.success(userDTOs));
        } catch (Exception e) {
            log.error("Error fetching users: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error fetching users"));
        }
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(@PathVariable Long id) {
        try {
            log.debug("Admin fetching user: {}", id);
            User user = userService.getUserById(id);
            return ResponseEntity.ok(ApiResponse.success(new UserDTO(user)));
        } catch (Exception e) {
            log.error("Error fetching user: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable Long id) {
        try {
            log.info("Admin deleting user: {}", id);
            userService.deleteUser(id);
            return ResponseEntity.ok(ApiResponse.success("User deleted", ""));
        } catch (Exception e) {
            log.error("Error deleting user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error deleting user"));
        }
    }

    // ============== DASHBOARD STATS ==============

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<DashboardStatsDTO>> getDashboardStats() {
        try {
            log.debug("Admin fetching dashboard statistics");
            DashboardStatsDTO stats = new DashboardStatsDTO();
            stats.setTotalUsers(userService.getTotalUsers());
            stats.setTotalOrders(orderService.getTotalOrders());
            stats.setTotalProducts(productService.getTotalProducts());
            stats.setTotalCategories(categoryService.getTotalCategories());
            stats.setTotalRevenue(orderService.getTotalRevenue());
            stats.setPendingOrders(orderService.getOrderCountByStatus("PENDING"));
            stats.setConfirmedOrders(orderService.getOrderCountByStatus("CONFIRMED"));
            
            return ResponseEntity.ok(ApiResponse.success(stats));
        } catch (Exception e) {
            log.error("Error fetching dashboard stats: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error fetching statistics"));
        }
    }
}
