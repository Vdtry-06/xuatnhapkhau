package com.xnk.supplier.controller;

import com.xnk.supplier.dto.SupplierDTO.SupplierRequest;
import com.xnk.supplier.dto.SupplierDTO.SupplierResponse;
import com.xnk.supplier.dto.SupplierInventoryDTO.SupplierInventoryRequest;
import com.xnk.supplier.dto.SupplierInventoryDTO.SupplierInventoryResponse;
import com.xnk.supplier.enums.Status;
import com.xnk.supplier.service.SupplierService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/suppliers")
public class SupplierController {

    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    @PostMapping
    public ResponseEntity<SupplierResponse> createSupplier(@Valid @RequestBody SupplierRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(supplierService.createSupplier(request));
    }

    @GetMapping
    public ResponseEntity<List<SupplierResponse>> getAllSuppliers() {
        return ResponseEntity.ok(supplierService.getAllSuppliers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SupplierResponse> getSupplierById(@PathVariable Long id) {
        return ResponseEntity.ok(supplierService.getSupplierById(id));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<SupplierResponse> updateSupplierStatus(
            @PathVariable Long id, @RequestParam Status status) {
        return ResponseEntity.ok(supplierService.updateSupplierStatus(id, status));
    }

    @GetMapping("/by-status")
    public ResponseEntity<List<SupplierResponse>> getByStatus(@RequestParam Status status) {
        return ResponseEntity.ok(supplierService.getSuppliersByStatus(status));
    }

    @PutMapping("/{id}/inventory")
    public ResponseEntity<SupplierInventoryResponse> upsertInventory(
            @PathVariable Long id,
            @Valid @RequestBody SupplierInventoryRequest request) {
        return ResponseEntity.ok(supplierService.upsertInventory(id, request));
    }

    @GetMapping("/{id}/inventory")
    public ResponseEntity<List<SupplierInventoryResponse>> getInventoryBySupplier(@PathVariable Long id) {
        return ResponseEntity.ok(supplierService.getInventoriesBySupplier(id));
    }

    @GetMapping("/inventory/by-product/{productId}")
    public ResponseEntity<List<SupplierInventoryResponse>> getInventoryByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(supplierService.getInventoriesByProduct(productId));
    }
}
