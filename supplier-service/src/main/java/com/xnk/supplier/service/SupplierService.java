package com.xnk.supplier.service;

import com.xnk.supplier.dto.SupplierDTO.SupplierRequest;
import com.xnk.supplier.dto.SupplierDTO.SupplierResponse;
import com.xnk.supplier.dto.SupplierInventoryDTO.SupplierInventoryRequest;
import com.xnk.supplier.dto.SupplierInventoryDTO.SupplierInventoryResponse;
import com.xnk.supplier.entity.SupplierInventory;
import com.xnk.supplier.entity.Supplier;
import com.xnk.supplier.repository.SupplierInventoryRepository;
import com.xnk.supplier.enums.Status;
import com.xnk.supplier.repository.SupplierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final SupplierInventoryRepository supplierInventoryRepository;

    public SupplierService(SupplierRepository supplierRepository,
                           SupplierInventoryRepository supplierInventoryRepository) {
        this.supplierRepository = supplierRepository;
        this.supplierInventoryRepository = supplierInventoryRepository;
    }

    public SupplierResponse createSupplier(SupplierRequest request) {
        if (supplierRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalArgumentException("Email đã tồn tại: " + request.email());
        }
        if (supplierRepository.findByTaxCode(request.taxCode()).isPresent()) {
            throw new IllegalArgumentException("Mã số thuế đã tồn tại: " + request.taxCode());
        }

        Supplier supplier = Supplier.builder()
                .name(request.name())
                .email(request.email())
                .phone(request.phone())
                .address(request.address())
                .taxCode(request.taxCode())
                .status(Status.ACTIVE)
                .build();

        return toResponse(supplierRepository.save(supplier));
    }

    public SupplierResponse updateSupplierStatus(Long supplierId, Status status) {
        Supplier supplier = findSupplier(supplierId);
        supplier.setStatus(status);
        return toResponse(supplierRepository.save(supplier));
    }

    @Transactional(readOnly = true)
    public SupplierResponse getSupplierById(Long supplierId) {
        return toResponse(findSupplier(supplierId));
    }

    @Transactional(readOnly = true)
    public List<SupplierResponse> getAllSuppliers() {
        return supplierRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<SupplierResponse> getSuppliersByStatus(Status status) {
        return supplierRepository.findByStatus(status).stream().map(this::toResponse).toList();
    }

    public SupplierInventoryResponse upsertInventory(Long supplierId, SupplierInventoryRequest request) {
        Supplier supplier = findSupplier(supplierId);
        SupplierInventory inventory = supplierInventoryRepository
                .findBySupplierIdAndProductId(supplierId, request.productId())
                .orElseGet(() -> SupplierInventory.builder()
                        .supplierId(supplierId)
                        .supplierName(supplier.getName())
                        .productId(request.productId())
                        .productName(request.productName())
                        .build());

        inventory.setSupplierName(supplier.getName());
        inventory.setProductName(request.productName());
        inventory.setAvailableQuantity(request.availableQuantity());
        inventory.setProductionCapacity(request.productionCapacity());
        inventory.setLeadTimeDays(request.leadTimeDays());
        inventory.setSupplyPrice(request.supplyPrice());
        inventory.setCanFulfillExtra(request.canFulfillExtra());
        inventory.setNote(request.note());

        return toInventoryResponse(supplierInventoryRepository.save(inventory));
    }

    @Transactional(readOnly = true)
    public List<SupplierInventoryResponse> getInventoriesBySupplier(Long supplierId) {
        return supplierInventoryRepository.findBySupplierId(supplierId).stream()
                .map(this::toInventoryResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SupplierInventoryResponse> getInventoriesByProduct(Long productId) {
        return supplierInventoryRepository.findByProductId(productId).stream()
                .map(this::toInventoryResponse)
                .toList();
    }

    private Supplier findSupplier(Long supplierId) {
        return supplierRepository.findById(supplierId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhà cung cấp id: " + supplierId));
    }

    private SupplierResponse toResponse(Supplier supplier) {
        return new SupplierResponse(
                supplier.getId(),
                supplier.getName(),
                supplier.getEmail(),
                supplier.getPhone(),
                supplier.getAddress(),
                supplier.getTaxCode(),
                supplier.getStatus().name()
        );
    }

    private SupplierInventoryResponse toInventoryResponse(SupplierInventory inventory) {
        return new SupplierInventoryResponse(
                inventory.getId(),
                inventory.getSupplierId(),
                inventory.getSupplierName(),
                inventory.getProductId(),
                inventory.getProductName(),
                inventory.getAvailableQuantity(),
                inventory.getProductionCapacity(),
                inventory.getLeadTimeDays(),
                inventory.getSupplyPrice(),
                inventory.isCanFulfillExtra(),
                inventory.getNote()
        );
    }
}
