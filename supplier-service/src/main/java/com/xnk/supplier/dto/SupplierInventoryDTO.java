package com.xnk.supplier.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class SupplierInventoryDTO {

    public record SupplierInventoryRequest(
            @NotNull Long productId,
            @NotBlank String productName,
            @NotNull @Min(0) Integer availableQuantity,
            @NotNull @Min(0) Integer productionCapacity,
            @NotNull @Min(0) Integer leadTimeDays,
            @NotNull BigDecimal supplyPrice,
            @NotNull Boolean canFulfillExtra,
            String note
    ) {}

    public record SupplierInventoryResponse(
            Long id,
            Long supplierId,
            String supplierName,
            Long productId,
            String productName,
            Integer availableQuantity,
            Integer productionCapacity,
            Integer leadTimeDays,
            BigDecimal supplyPrice,
            boolean canFulfillExtra,
            String note
    ) {}
}
