package com.xnk.supplier.repository;

import com.xnk.supplier.entity.SupplierInventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SupplierInventoryRepository extends JpaRepository<SupplierInventory, Long> {
    List<SupplierInventory> findBySupplierId(Long supplierId);
    List<SupplierInventory> findByProductId(Long productId);
    Optional<SupplierInventory> findBySupplierIdAndProductId(Long supplierId, Long productId);
}
