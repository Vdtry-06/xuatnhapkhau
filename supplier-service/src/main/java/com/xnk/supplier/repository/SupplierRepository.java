package com.xnk.supplier.repository;

import com.xnk.supplier.entity.Supplier;
import com.xnk.supplier.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    Optional<Supplier> findByEmail(String email);

    Optional<Supplier> findByTaxCode(String taxCode);

    List<Supplier> findByStatus(Status status);
}
