package com.xnk.payment.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;

@FeignClient(name = "order-service", url = "${services.order-service}")
public interface OrderClient {

    @PutMapping("/api/orders/{id}/status")
    void updateOrderStatus(@PathVariable("id") Long id, @RequestParam("status") String status);

    @GetMapping("/api/orders/{id}")
    OrderResponse getOrderById(@PathVariable("id") Long id);

    record OrderResponse(
            Long id,
            Long agentId,
            Long supplierId,
            BigDecimal totalAmount,
            String status,
            List<OrderlineResponse> orderlines
    ) {
    }

    record OrderlineResponse(
            Long id,
            Long productId,
            String productName,
            Long supplierId,
            String supplierName,
            Integer quantity,
            BigDecimal unitPrice,
            BigDecimal subTotal
    ) {
    }
}
