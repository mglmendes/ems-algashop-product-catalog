package com.algaworks.algashop.product.catalog.domain.model.product;

import com.algaworks.algashop.product.catalog.infrastructure.persistence.config.MongoDBConfig;
import com.algaworks.algashop.product.catalog.infrastructure.persistence.dataload.DataLoadProperties;
import com.algaworks.algashop.product.catalog.infrastructure.persistence.dataload.DataLoader;
import com.algaworks.algashop.product.catalog.infrastructure.persistence.product.QuantityInStockAdjustmentMongoDBImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.context.annotation.Import;

import java.util.UUID;

@DataMongoTest
@Import(
        {MongoDBConfig.class,
        QuantityInStockAdjustmentMongoDBImpl.class,
        DataLoader.class,
        DataLoadProperties.class}
)
public class QuantityInStockAdjustmentIT {

    @Autowired
    private QuantityInStockAdjustment quantityInStockAdjustment;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private DataLoader dataLoader;

    private static UUID existingProduct = UUID.fromString("946cea3b-d11d-4f11-b88d-3089b4e74087");

    @BeforeEach
    void setUp() throws Exception {
        dataLoader.run(new DefaultApplicationArguments());
    }

    @Test
    public void shouldIncreaseQuantityInStock() {
        Product product = productRepository.findById(existingProduct).orElseThrow();

        quantityInStockAdjustment.increase(existingProduct, 25);
        quantityInStockAdjustment.increase(existingProduct, 25);

        Product updatedProduct = productRepository.findById(existingProduct).orElseThrow();

        Assertions.assertThat(product.getQuantityInStock()).isEqualTo(50);
        Assertions.assertThat(updatedProduct.getQuantityInStock()).isEqualTo(100);
    }

    @Test
    public void shouldDecreaseQuantityInStock() {
        Product product = productRepository.findById(existingProduct).orElseThrow();

        quantityInStockAdjustment.decrease(existingProduct, 25);

        Product updatedProduct = productRepository.findById(existingProduct).orElseThrow();

        Assertions.assertThat(product.getQuantityInStock()).isEqualTo(50);
        Assertions.assertThat(updatedProduct.getQuantityInStock()).isEqualTo(25);
    }

    @Test
    public void shouldNotDecreaseQuantity() {
        Assertions.assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> {
            quantityInStockAdjustment.decrease(existingProduct, 100);
        });

        Product product = productRepository.findById(existingProduct).orElseThrow();
        Assertions.assertThat(product.getQuantityInStock()).isEqualTo(50);
    }
}
