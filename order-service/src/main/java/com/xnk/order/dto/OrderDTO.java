package com.xnk.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class OrderDTO {

    public record OrderlineRequest(
            @NotNull(message = "productId không được để trống")
            Long productId,

            @NotNull(message = "Số lượng không được để trống")
            @Min(value = 1, message = "Số lượng phải ít nhất là 1")
            Integer quantity
    ) {}

    public record OrderRequest(
            @NotNull(message = "agentId không được để trống")
            Long agentId,

            @NotBlank(message = "Địa chỉ giao hàng không được để trống")
            String shippingAddress,

            @NotEmpty(message = "Đơn hàng phải có ít nhất 1 sản phẩm")
            @Valid
            List<OrderlineRequest> orderlines
    ) {}

    public record OrderlineResponse(
            Long id,
            Long productId,
            String productName,
            Long supplierId,
            String supplierName,
            Integer quantity,
            BigDecimal unitPrice,
            BigDecimal subTotal,
            List<SupplierOptionResponse> supplierOptions
    ) {}

    public record SupplierOptionRequest(
            @NotNull Long supplierId,
            @NotBlank String supplierName,
            @NotNull @Min(0) Integer availableQuantity,
            @NotNull @Min(0) Integer productionQuantity,
            LocalDate expectedReadyDate,
            @NotBlank String status,
            String note
    ) {}

    public record SupplierOptionResponse(
            Long id,
            Long supplierId,
            String supplierName,
            Integer availableQuantity,
            Integer productionQuantity,
            LocalDate expectedReadyDate,
            String status,
            String note,
            boolean selected,
            boolean canceledByAgent
    ) {}

    public record OrderResponse(
            Long id,
            Long agentId,
            LocalDateTime orderDate,
            BigDecimal totalAmount,
            String status,
            Long supplierId,
            String supplierName,
            String shippingAddress,
            List<OrderlineResponse> orderlines
    ) {}
}
