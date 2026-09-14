package com.smartstore.smartstore.controller;

import com.smartstore.smartstore.config.CustomUserDetails;
import com.smartstore.smartstore.dto.*;
import com.smartstore.smartstore.entity.User;
import com.smartstore.smartstore.service.CartService;
import com.smartstore.smartstore.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cart")
@Slf4j
public class CartRestController {

    private final CartService cartService;
    private final UserService userService;

    public CartRestController(CartService cartService, UserService userService) {
        this.cartService = cartService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CartItemDTO>>> getCart(Authentication authentication) {
        try {
            log.debug("Fetching cart for user");
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = userService.getUserById(userDetails.getId());
            
            var cartItems = cartService.getCartItems(user.getId()).stream()
                    .map(CartItemDTO::new)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(ApiResponse.success(cartItems));
        } catch (Exception e) {
            log.error("Error fetching cart: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error fetching cart"));
        }
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<CartItemDTO>> addToCart(
            @RequestBody AddToCartRequest request,
            Authentication authentication) {
        try {
            log.info("Adding product {} to cart, quantity: {}", request.getProductId(), request.getQuantity());
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = userService.getUserById(userDetails.getId());
            
            var cartItem = cartService.addToCart(user.getId(), request.getProductId(), request.getQuantity(), user);
            
            return ResponseEntity.ok(ApiResponse.success("Item added to cart", new CartItemDTO(cartItem)));
        } catch (IllegalArgumentException e) {
            log.warn("Failed to add to cart: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error adding to cart: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error adding to cart"));
        }
    }

    @PutMapping("/{cartItemId}")
    public ResponseEntity<ApiResponse<CartItemDTO>> updateCartItem(
            @PathVariable Long cartItemId,
            @RequestBody AddToCartRequest request) {
        try {
            log.info("Updating cart item: {}, quantity: {}", cartItemId, request.getQuantity());
            var cartItem = cartService.updateCartItem(cartItemId, request.getQuantity());
            return ResponseEntity.ok(ApiResponse.success("Cart item updated", new CartItemDTO(cartItem)));
        } catch (IllegalArgumentException e) {
            log.warn("Failed to update cart item: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<ApiResponse<String>> removeFromCart(@PathVariable Long cartItemId) {
        try {
            log.info("Removing cart item: {}", cartItemId);
            cartService.removeFromCart(cartItemId);
            return ResponseEntity.ok(ApiResponse.success("Item removed from cart", ""));
        } catch (Exception e) {
            log.error("Error removing from cart: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error removing from cart"));
        }
    }

    @GetMapping("/total")
    public ResponseEntity<ApiResponse<BigDecimal>> getCartTotal(Authentication authentication) {
        try {
            log.debug("Fetching cart total");
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = userService.getUserById(userDetails.getId());
            BigDecimal total = cartService.getCartTotal(user.getId());
            return ResponseEntity.ok(ApiResponse.success(total));
        } catch (Exception e) {
            log.error("Error fetching cart total: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error fetching cart total"));
        }
    }

    @DeleteMapping("/clear")
    public ResponseEntity<ApiResponse<String>> clearCart(Authentication authentication) {
        try {
            log.info("Clearing cart");
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = userService.getUserById(userDetails.getId());
            cartService.clearCart(user.getId());
            return ResponseEntity.ok(ApiResponse.success("Cart cleared", ""));
        } catch (Exception e) {
            log.error("Error clearing cart: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error clearing cart"));
        }
    }
}
