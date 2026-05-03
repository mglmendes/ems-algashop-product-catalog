package com.algaworks.algashop.product.catalog.presentation.controller;

import com.algaworks.algashop.product.catalog.application.product.input.ProductInput;
import com.algaworks.algashop.product.catalog.application.product.output.ProductDetailOutput;
import com.algaworks.algashop.product.catalog.application.product.output.ProductSummaryOutput;
import com.algaworks.algashop.product.catalog.application.product.service.query.ProductFilter;
import com.algaworks.algashop.product.catalog.application.product.service.query.ProductQueryService;
import com.algaworks.algashop.product.catalog.application.product.service.management.ProductManagementApplicationService;
import com.algaworks.algashop.product.catalog.domain.model.category.CategoryNotFoundException;
import com.algaworks.algashop.product.catalog.presentation.exceptionhandler.UnprocessableContentException;
import com.algaworks.algashop.product.catalog.presentation.model.PageModel;
import com.algaworks.algashop.product.catalog.presentation.model.ProductQuantityModel;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductQueryService productQueryService;
    private final ProductManagementApplicationService productManagementApplicationService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ProductDetailOutput create(@RequestBody @Valid ProductInput productInput) {
        UUID productId;
        try {
            productId = productManagementApplicationService.create(productInput);
        } catch (CategoryNotFoundException e) {
            throw new UnprocessableContentException(e.getMessage());

        }
        return productQueryService.findById(productId);
    }

    @GetMapping("/{productId}")
    public ProductDetailOutput getProduct(@PathVariable UUID productId) {
        return productQueryService.findById(productId);

    }

    @GetMapping
    public PageModel<ProductSummaryOutput> filter(ProductFilter productFilter) {
        return productQueryService.filter(productFilter);
    }

    @PutMapping("/{productId}")
    public ProductDetailOutput update(@PathVariable UUID productId,
                                      @RequestBody @Valid ProductInput input) {
        productManagementApplicationService.update(productId, input);
        return productQueryService.findById(productId);
    }

    @PutMapping("/{productId}/enable")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void enable(@PathVariable UUID productId) {
        productManagementApplicationService.enable(productId);
    }

    @DeleteMapping("/{productId}/enable")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID productId) {
        productManagementApplicationService.disable(productId);
    }

    @PostMapping("/{productId}/restock")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void restock(@PathVariable UUID productId, @Valid @RequestBody ProductQuantityModel quantity) {
        productManagementApplicationService.restock(productId, quantity.getQuantity());
    }

    @PostMapping("/{productId}/withdraw")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void withdraw(@PathVariable UUID productId, @Valid @RequestBody ProductQuantityModel quantity) {
        productManagementApplicationService.withdraw(productId, quantity.getQuantity());
    }
}
