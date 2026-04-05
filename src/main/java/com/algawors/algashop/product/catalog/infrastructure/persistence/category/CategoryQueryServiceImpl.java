package com.algawors.algashop.product.catalog.infrastructure.persistence.category;

import com.algawors.algashop.product.catalog.application.category.output.CategoryDetailOutput;
import com.algawors.algashop.product.catalog.application.category.service.CategoryQueryService;
import com.algawors.algashop.product.catalog.application.utility.Mapper;
import com.algawors.algashop.product.catalog.domain.model.category.Category;
import com.algawors.algashop.product.catalog.domain.model.category.CategoryRepository;
import com.algawors.algashop.product.catalog.presentation.model.PageModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryQueryServiceImpl implements CategoryQueryService {

    private final CategoryRepository categoryRepository;
    private final Mapper mapper;

    @Override
    public PageModel<CategoryDetailOutput> filter(Integer size, Integer number) {
        return null;
    }

    @Override
    public CategoryDetailOutput findById(UUID categoryId) {
        Category category = categoryRepository.findById(categoryId).orElseThrow(
                () -> new IllegalArgumentException()
        );
        return mapper.convert(category, CategoryDetailOutput.class);
    }
}
