package com.xnk.order.repository;

import com.xnk.order.entity.OrderlineSupplierResponse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderlineSupplierResponseRepository extends JpaRepository<OrderlineSupplierResponse, Long> {
    List<OrderlineSupplierResponse> findByOrderlineId(Long orderlineId);
    Optional<OrderlineSupplierResponse> findByOrderlineIdAndSupplierId(Long orderlineId, Long supplierId);
    List<OrderlineSupplierResponse> findBySupplierId(Long supplierId);
}
