package com.xnk.product.controller;

import com.xnk.product.dto.ProductDTO.*;
import com.xnk.product.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(productService.getProductSummary(id));
    }

    /** Internal — Order Service gọi để trừ tồn kho */
    @PutMapping("/{id}/stock")
    public ResponseEntity<Boolean> updateStock(
            @PathVariable("id") Long id,
            @RequestParam("quantityToDeduct") Integer quantityToDeduct) {
        return ResponseEntity.ok(productService.updateStock(id, quantityToDeduct));
    }

    /** Internal — Order Service gọi để lấy giá + kiểm tra tồn kho */
    @GetMapping("/{id}/stock-check")
    public ResponseEntity<StockCheckResponse> checkStock(
            @PathVariable("id") Long id,
            @RequestParam("quantity") Integer quantity) {
        return ResponseEntity.ok(productService.checkStock(id, quantity));
    }
}
