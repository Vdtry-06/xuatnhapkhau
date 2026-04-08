package com.xnk.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@FeignClient(name = "product-service", url = "${services.product-service}")
public interface ProductClient {



    @GetMapping("/api/products/{id}/stock-check")
    StockCheckResponse checkStock(@PathVariable("id") Long id, @RequestParam("quantity") Integer quantity);

    @GetMapping("/api/products/{id}")
    ProductResponse getProductById(@PathVariable("id") Long id);

    @PutMapping("/api/products/{id}/stock")
    boolean updateStock(@PathVariable("id") Long id, @RequestParam("quantityToDeduct") Integer quantityToDeduct);

    record StockCheckResponse(
            Long productId,
            String productName,
            BigDecimal exportPrice,
            Integer stockQuantity,
            Long supplierId,
            String supplierName,
            boolean available
    ) {}

    record ProductResponse(
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
}
