package com.smartstore.smartstore.controller;

import com.smartstore.smartstore.entity.Order;
import com.smartstore.smartstore.entity.User;
import com.smartstore.smartstore.service.OrderService;
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
@RequestMapping("/orders")
@Slf4j
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;

    public OrderController(OrderService orderService, UserService userService) {
        this.orderService = orderService;
        this.userService = userService;
    }

    @GetMapping
    public String listUserOrders(
            @RequestParam(defaultValue = "0") int page,
            Authentication authentication,
            Model model) {
        
        log.debug("Fetching user orders - page: {}", page);
        User user = userService.getUserByUsername(authentication.getName());
        Pageable pageable = PageRequest.of(page, 10);
        Page<Order> orders = orderService.getUserOrders(user.getId(), pageable);
        
        model.addAttribute("orders", orders);
        return "orders/list";
    }

    @GetMapping("/{id}")
    public String viewOrder(@PathVariable Long id, Authentication authentication, Model model) {
        log.debug("Viewing order: {}", id);
        Order order = orderService.getOrderById(id);
        
        // Verify order belongs to current user
        User user = userService.getUserByUsername(authentication.getName());
        if (!order.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Unauthorized access to order");
        }
        
        model.addAttribute("order", order);
        return "orders/detail";
    }

    @GetMapping("/checkout")
    public String checkout(Model model) {
        model.addAttribute("order", new Order());
        return "orders/checkout";
    }

    @PostMapping("/create")
    public String createOrder(
            @RequestParam String shippingAddress,
            @RequestParam String phoneNumber,
            @RequestParam(required = false) String notes,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        
        try {
            log.info("Creating order");
            User user = userService.getUserByUsername(authentication.getName());
            Order order = orderService.createOrder(user, shippingAddress, phoneNumber, notes);
            redirectAttributes.addFlashAttribute("success", "Order created successfully! Please proceed with payment.");
            return "redirect:/orders/" + order.getId();
        } catch (IllegalArgumentException e) {
            log.warn("Failed to create order: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/cart";
        }
    }
}
