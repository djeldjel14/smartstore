package com.smartstore.smartstore.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDTO {
    private Long totalUsers;
    private Long totalOrders;
    private Long totalProducts;
    private Long totalCategories;
    private Double totalRevenue;
    private Long pendingOrders;
    private Long confirmedOrders;
}
