package com.algawors.algashop.product.catalog.presentation.controller;

import com.algawors.algashop.product.catalog.application.product.input.ProductInput;
import com.algawors.algashop.product.catalog.application.product.output.ProductDetailOutput;
import com.algawors.algashop.product.catalog.application.product.service.query.ProductQueryService;
import com.algawors.algashop.product.catalog.application.product.service.management.ProductManagementApplicationService;
import com.algawors.algashop.product.catalog.domain.model.category.CategoryNotFoundException;
import com.algawors.algashop.product.catalog.presentation.exceptionhandler.UnprocessableContentException;
import com.algawors.algashop.product.catalog.presentation.model.PageModel;
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
    public PageModel<ProductDetailOutput> filter(
            @RequestParam(name = "size", required = false) Integer size,
            @RequestParam(name = "number", required = false) Integer number
    ) {
        return productQueryService.filter(size, number);
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
}
