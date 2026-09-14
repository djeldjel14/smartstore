package com.smartstore.smartstore.controller;

import com.smartstore.smartstore.config.CustomUserDetails;
import com.smartstore.smartstore.dto.*;
import com.smartstore.smartstore.entity.Order;
import com.smartstore.smartstore.entity.User;
import com.smartstore.smartstore.service.OrderService;
import com.smartstore.smartstore.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
@Slf4j
public class OrderRestController {

    private final OrderService orderService;
    private final UserService userService;

    public OrderRestController(OrderService orderService, UserService userService) {
        this.orderService = orderService;
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OrderDTO>> createOrder(
            @RequestBody CreateOrderRequest request,
            Authentication authentication) {
        try {
            log.info("Creating new order");
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = userService.getUserById(userDetails.getId());
            
            Order order = orderService.createOrder(user, request.getShippingAddress(), request.getPhoneNumber(), request.getNotes());
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Order created successfully", new OrderDTO(order)));
        } catch (IllegalArgumentException e) {
            log.warn("Failed to create order: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error creating order: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error creating order"));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderDTO>>> getUserOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        try {
            log.debug("Fetching user orders - page: {}, size: {}", page, size);
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = userService.getUserById(userDetails.getId());
            
            Pageable pageable = PageRequest.of(page, size);
            Page<Order> orders = orderService.getUserOrders(user.getId(), pageable);
            
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

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderDTO>> getOrderById(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            log.debug("Fetching order: {}", id);
            Order order = orderService.getOrderById(id);
            
            // Verify order belongs to current user
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = userService.getUserById(userDetails.getId());
            if (!order.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("Unauthorized access to this order"));
            }
            
            return ResponseEntity.ok(ApiResponse.success(new OrderDTO(order)));
        } catch (IllegalArgumentException e) {
            log.warn("Order not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error fetching order: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error fetching order"));
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<OrderDTO>> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        try {
            log.info("Updating order {} status to: {}", id, status);
            Order order = orderService.updateOrderStatus(id, status);
            return ResponseEntity.ok(ApiResponse.success("Order status updated", new OrderDTO(order)));
        } catch (IllegalArgumentException e) {
            log.warn("Failed to update order: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error updating order: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error updating order"));
        }
    }

    @GetMapping("/cancel/{id}")
    public ResponseEntity<ApiResponse<OrderDTO>> cancelOrder(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            log.info("Cancelling order: {}", id);
            Order order = orderService.getOrderById(id);
            
            // Verify order belongs to current user
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = userService.getUserById(userDetails.getId());
            if (!order.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("Unauthorized access to this order"));
            }
            
            Order cancelledOrder = orderService.cancelOrder(id);
            return ResponseEntity.ok(ApiResponse.success("Order cancelled", new OrderDTO(cancelledOrder)));
        } catch (IllegalArgumentException e) {
            log.warn("Failed to cancel order: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error cancelling order: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error cancelling order"));
        }
    }
}
