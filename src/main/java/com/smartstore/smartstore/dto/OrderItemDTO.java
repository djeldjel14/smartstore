package com.smartstore.smartstore.dto;

import com.smartstore.smartstore.entity.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDTO {
    private Long id;
    private Long productId;
    private String productName;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal subtotal;

    public OrderItemDTO(OrderItem item) {
        this.id = item.getId();
        this.productId = item.getProduct().getId();
        this.productName = item.getProduct().getName();
        this.price = item.getPrice();
        this.quantity = item.getQuantity();
        this.subtotal = item.getSubtotal();
    }
}
