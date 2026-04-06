package com.algawors.algashop.product.catalog.application.product.service.management;

import com.algawors.algashop.product.catalog.application.exception.ResourceNotFoundException;
import com.algawors.algashop.product.catalog.application.product.input.ProductInput;
import com.algawors.algashop.product.catalog.domain.model.category.Category;
import com.algawors.algashop.product.catalog.domain.model.category.CategoryNotFoundException;
import com.algawors.algashop.product.catalog.domain.model.category.CategoryRepository;
import com.algawors.algashop.product.catalog.domain.model.product.Product;
import com.algawors.algashop.product.catalog.domain.model.product.ProductNotFoundException;
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
        Product product = findProduct(productId);
        Category category = findCategory(productInput.getCategoryId());
        updateProduct(product, productInput);
        productRepository.save(product);
    }

    public void disable(UUID productId) {
        Product product = findProduct(productId);
        product.disable();
        productRepository.save(product);
    }

    public void enable(UUID productId) {
        Product product = findProduct(productId);
        product.enable();
        productRepository.save(product);
    }

    private Product mapToProduct(ProductInput productInput) {
        Category category = findCategory(productInput.getCategoryId());
        return Product.builder()
                .name(productInput.getName())
                .brand(productInput.getBrand())
                .description(productInput.getDescription())
                .regularPrice(productInput.getRegularPrice())
                .salePrice(productInput.getSalePrice())
                .enabled(productInput.getEnabled())
                .build();
    }

    private void updateProduct(Product product, ProductInput productInput) {
        product.setName(productInput.getName());
        product.setBrand(productInput.getBrand());
        product.setDescription(productInput.getDescription());
        product.setEnabled(productInput.getEnabled());
        product.setRegularPrice(productInput.getRegularPrice());
        product.setSalePrice(productInput.getSalePrice());
    }

    private Product findProduct(UUID productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
    }

    private Category findCategory(UUID categoryId) {
        return categoryRepository.findById(categoryId).orElseThrow(
                () -> new CategoryNotFoundException(categoryId)
        );
    }
}
