package com.algawors.algashop.product.catalog.application.product.service;

import com.algawors.algashop.product.catalog.application.exception.ResourceNotFoundException;
import com.algawors.algashop.product.catalog.application.product.input.ProductInput;
import com.algawors.algashop.product.catalog.domain.model.category.Category;
import com.algawors.algashop.product.catalog.domain.model.category.CategoryRepository;
import com.algawors.algashop.product.catalog.domain.model.product.Product;
import com.algawors.algashop.product.catalog.domain.model.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductManagementApplicationService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public UUID create(ProductInput productInput) {
        Product product = mapToProduct(productInput);
        productRepository.save(product);
        return product.getId();
    }

    public void update(UUID productId, ProductInput productInput) {
    }

    public void disable(UUID productId) {

    }


    private Product mapToProduct(ProductInput productInput) {
        Category category = findCategory(productInput);
        return Product.builder()
                .name(productInput.getName())
                .brand(productInput.getBrand())
                .description(productInput.getDescription())
                .regularPrice(productInput.getRegularPrice())
                .salePrice(productInput.getSalePrice())
                .enabled(productInput.getEnabled())
                .build();
    }

    private Category findCategory(ProductInput productInput) {
        return categoryRepository.findById(productInput.getCategoryId()).orElseThrow(
                ResourceNotFoundException::new
        );
    }
}
