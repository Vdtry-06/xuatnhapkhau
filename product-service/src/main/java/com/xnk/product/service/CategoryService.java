package com.xnk.product.service;

import com.xnk.product.dto.ProductDTO.CategoryDTO;
import com.xnk.product.dto.ProductDTO.CategoryRequest;
import com.xnk.product.entity.Category;
import com.xnk.product.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public CategoryDTO createCategory(CategoryRequest request) {
        if (categoryRepository.findByName(request.name()).isPresent()) {
            throw new IllegalArgumentException("Tên danh mục đã tồn tại: " + request.name());
        }
        Category category = Category.builder()
                .name(request.name())
                .description(request.description())
                .build();
        return toDTO(categoryRepository.save(category));
    }

    @Transactional(readOnly = true)
    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findAll().stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<CategoryDTO> searchCategories(String keyword) {
        return categoryRepository.findByNameContaining(keyword).stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public Category getCategoryEntityById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy danh mục id: " + id));
    }

    private CategoryDTO toDTO(Category c) {
        return new CategoryDTO(c.getId(), c.getName(), c.getDescription());
    }
}
