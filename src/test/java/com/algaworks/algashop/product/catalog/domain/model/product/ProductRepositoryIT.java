package com.algaworks.algashop.product.catalog.domain.model.product;

import com.algaworks.algashop.product.catalog.infrastructure.persistence.config.MongoDBConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

@DataMongoTest
@Import(MongoDBConfig.class)
@Slf4j
class ProductRepositoryIT {

    @Autowired
    private ProductRepository productRepository;

    @Test
    public void shouldFilter() {
        Page<ProductNameProjection> products = productRepository.findAllByEnabled(true, PageRequest.of(0, 3));
        products.forEach(p -> log.info("Product ID: {} - Name: {}", p.id(), p.name()));

    }

}