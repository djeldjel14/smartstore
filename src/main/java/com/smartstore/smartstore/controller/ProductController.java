package com.smartstore.smartstore.controller;

import com.smartstore.smartstore.entity.Product;
import com.smartstore.smartstore.entity.User;
import com.smartstore.smartstore.service.CartService;
import com.smartstore.smartstore.service.ProductService;
import com.smartstore.smartstore.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/products")
@Slf4j
public class ProductController {

    private final ProductService productService;
    private final CartService cartService;
    private final UserService userService;

    public ProductController(ProductService productService, CartService cartService, UserService userService) {
        this.productService = productService;
        this.cartService = cartService;
        this.userService = userService;
    }

    @GetMapping
    public String listProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String search,
            Model model) {
        
        log.debug("Listing products - page: {}, size: {}, search: {}", page, size, search);
        Pageable pageable = PageRequest.of(page, size);
        
        Page<Product> products;
        if (search != null && !search.isEmpty()) {
            products = productService.searchProducts(search, pageable);
        } else {
            products = productService.getAllProducts(pageable);
        }

        model.addAttribute("products", products);
        model.addAttribute("search", search);
        return "products/list";
    }

    @GetMapping("/{id}")
    public String viewProduct(@PathVariable Long id, Model model) {
        log.debug("Viewing product: {}", id);
        Product product = productService.getProductById(id);
        model.addAttribute("product", product);
        return "products/detail";
    }

    @PostMapping("/{id}/add-to-cart")
    public String addToCart(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") Integer quantity,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        
        try {
            log.info("Adding product {} to cart, quantity: {}", id, quantity);
            User user = userService.getUserByUsername(authentication.getName());
            cartService.addToCart(user.getId(), id, quantity, user);
            redirectAttributes.addFlashAttribute("success", "Product added to cart!");
        } catch (IllegalArgumentException e) {
            log.warn("Failed to add to cart: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/products/" + id;
    }
}
