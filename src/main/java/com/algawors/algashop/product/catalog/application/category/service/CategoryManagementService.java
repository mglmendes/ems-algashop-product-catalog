package com.algawors.algashop.product.catalog.application.category.service;

import com.algawors.algashop.product.catalog.application.category.input.CategoryInput;
import com.algawors.algashop.product.catalog.application.exception.ResourceNotFoundException;
import com.algawors.algashop.product.catalog.domain.model.category.Category;
import com.algawors.algashop.product.catalog.domain.model.category.CategoryRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryManagementService {

    private final CategoryRepository categoryRepository;

    public UUID create(@Valid CategoryInput input) {
        Category category = new Category(input.getName(), input.getEnabled());
        return categoryRepository.save(category).getId();
    }

    public void update(UUID categoryId, CategoryInput input) {
        Category category = categoryRepository.findById(categoryId).orElseThrow(
                () -> new ResourceNotFoundException()
        );
        category.setName(input.getName());
        category.setEnabled(input.getEnabled());
        categoryRepository.save(category);
    }

    public void disable(UUID categoryId) {
        Category category = categoryRepository.findById(categoryId).orElseThrow(
                () -> new ResourceNotFoundException()
        );
        category.setEnabled(false);
        categoryRepository.save(category);
    }
}