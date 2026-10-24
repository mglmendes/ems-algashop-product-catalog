package com.algaworks.algashop.product.catalog.presentation.controller;

import com.algaworks.algashop.product.catalog.application.product.input.ProductInput;
import com.algaworks.algashop.product.catalog.application.product.output.ProductDetailOutput;
import com.algaworks.algashop.product.catalog.application.product.output.ProductSummaryOutput;
import com.algaworks.algashop.product.catalog.application.product.service.management.ProductManagementApplicationService;
import com.algaworks.algashop.product.catalog.application.product.service.query.ProductFilter;
import com.algaworks.algashop.product.catalog.application.product.service.query.ProductQueryService;
import com.algaworks.algashop.product.catalog.domain.model.category.CategoryNotFoundException;
import com.algaworks.algashop.product.catalog.infrastructure.security.SecurityAnnotations.CanReadProducts;
import com.algaworks.algashop.product.catalog.infrastructure.security.SecurityAnnotations.CanWriteProducts;
import com.algaworks.algashop.product.catalog.infrastructure.security.SecurityAnnotations.CanWriteProductsStock;
import com.algaworks.algashop.product.catalog.presentation.exceptionhandler.UnprocessableContentException;
import com.algaworks.algashop.product.catalog.presentation.model.PageModel;
import com.algaworks.algashop.product.catalog.presentation.model.ProductQuantityModel;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductQueryService productQueryService;
    private final ProductManagementApplicationService productManagementApplicationService;

    @CanWriteProducts
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductDetailOutput create(@RequestBody @Valid ProductInput productInput) {
        try {
            return productManagementApplicationService.create(productInput);
        } catch (CategoryNotFoundException e) {
            throw new UnprocessableContentException(e.getMessage());

        }
    }

    @CanReadProducts
    @GetMapping("/{productId}")
    public ResponseEntity<ProductDetailOutput> getProduct(@PathVariable UUID productId) {
        ProductDetailOutput product = productQueryService.findById(productId);
        return ResponseEntity.ok()
                .cacheControl(CacheControl
                        .maxAge(Duration.ofMinutes(1))
                        .cachePublic())
                .eTag("product:id:" + product.getId() + ":v:" + product.getVersion())
                .lastModified(product.getUpdatedAt().toInstant())
                .body(product);
    }

    @CanReadProducts
    @GetMapping
    public PageModel<ProductSummaryOutput> filter(ProductFilter productFilter) {
        return productQueryService.filter(productFilter);
    }

    @CanWriteProducts
    @PutMapping("/{productId}")
    public ProductDetailOutput update(@PathVariable UUID productId,
                                      @RequestBody @Valid ProductInput input) {
        return productManagementApplicationService.update(productId, input);
    }

    @CanWriteProducts
    @PutMapping("/{productId}/enable")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void enable(@PathVariable UUID productId) {
        productManagementApplicationService.enable(productId);
    }

    @CanWriteProducts
    @DeleteMapping("/{productId}/enable")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID productId) {
        productManagementApplicationService.disable(productId);
    }

    @CanWriteProductsStock
    @PostMapping("/{productId}/restock")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void restock(@PathVariable UUID productId, @Valid @RequestBody ProductQuantityModel quantity) {
        productManagementApplicationService.restock(productId, quantity.getQuantity());
    }

    @CanWriteProductsStock
    @PostMapping("/{productId}/withdraw")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void withdraw(@PathVariable UUID productId, @Valid @RequestBody ProductQuantityModel quantity) {
        productManagementApplicationService.withdraw(productId, quantity.getQuantity());
    }
}
