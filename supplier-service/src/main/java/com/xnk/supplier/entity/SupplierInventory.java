package com.xnk.supplier.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "supplier_inventory")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long supplierId;

    @Column(nullable = false)
    private String supplierName;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private String productName;

    @Column(nullable = false)
    private Integer availableQuantity;

    @Column(nullable = false)
    private Integer productionCapacity;

    @Column(nullable = false)
    private Integer leadTimeDays;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal supplyPrice;

    @Column(nullable = false)
    private boolean canFulfillExtra;

    private String note;
}
