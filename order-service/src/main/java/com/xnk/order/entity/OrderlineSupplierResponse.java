package com.xnk.order.entity;

import com.xnk.order.enums.SupplierResponseStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "orderline_supplier_responses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderlineSupplierResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orderline_id", nullable = false)
    private Orderline orderline;

    @Column(nullable = false)
    private Long supplierId;

    @Column(nullable = false)
    private String supplierName;

    @Column(nullable = false)
    private Integer availableQuantity;

    @Column(nullable = false)
    private Integer productionQuantity;

    private LocalDate expectedReadyDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SupplierResponseStatus status;

    private String note;
}
