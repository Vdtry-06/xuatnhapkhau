package com.xnk.product.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class ProductDTO {

    public record CategoryDTO(
            Long id,
            String name,
            String description
    ) {}

    public record CategoryRequest(
            @NotBlank(message = "Tên danh mục không được để trống")
            String name,
            String description
    ) {}

    public record ProductRequest(
            @NotBlank(message = "SKU không được để trống")
            String sku,

            @NotBlank(message = "Tên sản phẩm không được để trống")
            String name,

            String description,

            @NotNull(message = "Giá nhập không được để trống")
            @DecimalMin(value = "0.0", inclusive = false, message = "Giá nhập phải lớn hơn 0")
            BigDecimal importPrice,

            @NotNull(message = "Giá xuất không được để trống")
            @DecimalMin(value = "0.0", inclusive = false, message = "Giá xuất phải lớn hơn 0")
            BigDecimal exportPrice,

            @NotNull(message = "Số lượng tồn kho không được để trống")
            @Min(value = 0, message = "Số lượng tồn kho không được âm")
            Integer stockQuantity,

            Long supplierId,

            String supplierName,

            @NotNull(message = "Danh mục không được để trống")
            Long categoryId
    ) {}

    public record ProductResponse(
            Long id,
            String sku,
            String name,
            String description,
            BigDecimal exportPrice,
            Integer stockQuantity,
            Long supplierId,
            String supplierName,
            Long categoryId,
            String categoryName
    ) {}

    // Dùng nội bộ cho Order Service check tồn kho
    public record StockCheckResponse(
            Long productId,
            String productName,
            BigDecimal exportPrice,
            Integer stockQuantity,
            Long supplierId,
            String supplierName,
            boolean available
    ) {}
}
