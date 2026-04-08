package com.xnk.product.service;

import com.xnk.product.dto.ProductDTO.*;
import com.xnk.product.entity.Category;
import com.xnk.product.entity.Product;
import com.xnk.product.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryService categoryService;

    public ProductService(ProductRepository productRepository, CategoryService categoryService) {
        this.productRepository = productRepository;
        this.categoryService = categoryService;
    }

    public ProductResponse createProduct(ProductRequest request) {
        if (productRepository.findBySku(request.sku()).isPresent()) {
            throw new IllegalArgumentException("SKU đã tồn tại: " + request.sku());
        }
        Category category = categoryService.getCategoryEntityById(request.categoryId());
        Product product = Product.builder()
                .sku(request.sku())
                .name(request.name())
                .description(request.description())
                .importPrice(request.importPrice())
                .exportPrice(request.exportPrice())
                .stockQuantity(request.stockQuantity())
                .supplierId(request.supplierId())
                .supplierName(request.supplierName())
                .category(category)
                .build();
        return toResponse(productRepository.save(product));
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long productId) {
        return toResponse(findProduct(productId));
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> searchProducts(String keyword) {
        return productRepository.findByNameContaining(keyword).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryId(categoryId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getProductsBySupplier(Long supplierId) {
        return productRepository.findBySupplierId(supplierId).stream().map(this::toResponse).toList();
    }

    public boolean updateStock(Long productId, Integer quantityToDeduct) {
        Product product = findProduct(productId);
        if (product.getStockQuantity() < quantityToDeduct) {
            return false;
        }
        product.setStockQuantity(product.getStockQuantity() - quantityToDeduct);
        productRepository.save(product);
        return true;
    }

    @Transactional(readOnly = true)
    public StockCheckResponse checkStock(Long productId, Integer quantity) {
        Product product = findProduct(productId);
        boolean available = product.getStockQuantity() >= quantity;
        return new StockCheckResponse(
                product.getId(),
                product.getName(),
                product.getExportPrice(),
                product.getStockQuantity(),
                product.getSupplierId(),
                product.getSupplierName(),
                available
        );
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductSummary(Long productId) {
        return toResponse(findProduct(productId));
    }

    private Product findProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm id: " + productId));
    }

    private ProductResponse toResponse(Product p) {
        return new ProductResponse(
                p.getId(),
                p.getSku(),
            p.getName(),
            p.getDescription(),
            p.getExportPrice(),
            p.getStockQuantity(),
            p.getSupplierId(),
            p.getSupplierName(),
            p.getCategory().getId(),
            p.getCategory().getName()
        );
    }
}
