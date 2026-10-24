package com.algaworks.algashop.product.catalog.presentation.controller;


import com.algaworks.algashop.product.catalog.application.category.input.CategoryInput;
import com.algaworks.algashop.product.catalog.application.category.output.CategoryDetailOutput;
import com.algaworks.algashop.product.catalog.application.category.service.management.CategoryManagementService;
import com.algaworks.algashop.product.catalog.application.category.service.query.CategoryFilter;
import com.algaworks.algashop.product.catalog.application.category.service.query.CategoryQueryService;
import com.algaworks.algashop.product.catalog.presentation.model.PageModel;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryQueryService categoryQueryService;
    private final CategoryManagementService categoryManagementService;

    @GetMapping
    public ResponseEntity<PageModel<CategoryDetailOutput>> filter(CategoryFilter categoryFilter, WebRequest request) {

        if (!categoryFilter.isCacheable()) {
            PageModel<CategoryDetailOutput> results = categoryQueryService.filter(categoryFilter);
            return ResponseEntity.ok(results);
        }

        OffsetDateTime lastModified = categoryQueryService.lastModified();

        if (request.checkNotModified(lastModified.toInstant().toEpochMilli())) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
        }

        PageModel<CategoryDetailOutput> results = categoryQueryService.filter(categoryFilter);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofMinutes(5)).cachePublic())
                .lastModified(lastModified.toInstant())
                .body(results);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDetailOutput create(@RequestBody @Valid CategoryInput input) {
        UUID categoryId = categoryManagementService.create(input);
        return categoryQueryService.findById(categoryId);
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryDetailOutput> findById(@PathVariable UUID categoryId) {
        CategoryDetailOutput category = categoryQueryService.findById(categoryId);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofMinutes(5)).cachePublic())
                .eTag("category:id:"+ category.getId() + ":v:" + category.getVersion())
                .lastModified(category.getUpdatedAt().toInstant())
                .body(category);
    }

    @PutMapping("/{categoryId}")
    public CategoryDetailOutput update(
            @PathVariable UUID categoryId,
            @RequestBody @Valid CategoryInput input) {
        categoryManagementService.update(categoryId, input);
        return categoryQueryService.findById(categoryId);
    }

    @DeleteMapping("/{categoryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void disable(@PathVariable UUID categoryId) {
        categoryManagementService.disable(categoryId);
    }
}