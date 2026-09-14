package com.smartstore.smartstore.controller;

import com.smartstore.smartstore.entity.CartItem;
import com.smartstore.smartstore.entity.User;
import com.smartstore.smartstore.service.CartService;
import com.smartstore.smartstore.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/cart")
@Slf4j
public class CartController {

    private final CartService cartService;
    private final UserService userService;

    public CartController(CartService cartService, UserService userService) {
        this.cartService = cartService;
        this.userService = userService;
    }

    @GetMapping
    public String viewCart(Authentication authentication, Model model) {
        log.debug("Viewing cart");
        User user = userService.getUserByUsername(authentication.getName());
        List<CartItem> cartItems = cartService.getCartItems(user.getId());
        BigDecimal total = cartService.getCartTotal(user.getId());
        
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("total", total);
        return "cart/view";
    }

    @PostMapping("/{cartItemId}/update")
    public String updateCartItem(
            @PathVariable Long cartItemId,
            @RequestParam Integer quantity,
            RedirectAttributes redirectAttributes) {
        
        try {
            log.info("Updating cart item: {}, quantity: {}", cartItemId, quantity);
            cartService.updateCartItem(cartItemId, quantity);
            redirectAttributes.addFlashAttribute("success", "Cart updated!");
        } catch (IllegalArgumentException e) {
            log.warn("Failed to update cart: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/cart";
    }

    @PostMapping("/{cartItemId}/remove")
    public String removeFromCart(
            @PathVariable Long cartItemId,
            RedirectAttributes redirectAttributes) {
        
        log.info("Removing item from cart: {}", cartItemId);
        cartService.removeFromCart(cartItemId);
        redirectAttributes.addFlashAttribute("success", "Item removed from cart!");
        return "redirect:/cart";
    }
}
