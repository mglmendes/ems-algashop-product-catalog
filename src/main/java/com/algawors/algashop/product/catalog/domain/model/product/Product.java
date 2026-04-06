package com.algawors.algashop.product.catalog.domain.model.product;

import com.algawors.algashop.product.catalog.domain.model.DomainException;
import com.algawors.algashop.product.catalog.domain.utility.IdGenerator;
import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Document(collection = "products")
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product {

    @Id
    @EqualsAndHashCode.Include
    private UUID id;

    private String name;

    private String brand;

    @Setter
    private String description;

    private Integer quantityInStock;

    private Boolean enabled;

    private BigDecimal regularPrice;

    private BigDecimal salePrice;

    @Version
    private Long version;

    @CreatedDate
    private OffsetDateTime addedAt;

    @LastModifiedDate
    private OffsetDateTime updatedAt;

    @CreatedBy
    private UUID createdByUserId;

    @LastModifiedBy
    private UUID modifiedByUserId;

    @Builder
    public Product(String name, String brand, String description, Boolean enabled,
                   BigDecimal regularPrice, BigDecimal salePrice) {
        setId(IdGenerator.generateTimeBasedUUID());
        setName(name);
        setBrand(brand);
        setDescription(description);
        setEnabled(enabled);
        setRegularPrice(regularPrice);
        setSalePrice(salePrice);
        setQuantityInStock(0);
    }

    public void setName(String name) {
        if (StringUtils.isEmpty(name)) {
            throw  new IllegalArgumentException("name cannot be empty");
        }
        this.name = name;
    }

    public void setBrand(String brand) {
        if (StringUtils.isEmpty(brand)) {
            throw  new IllegalArgumentException("brand cannot be empty");
        }
        this.brand = brand;
    }

    public void setRegularPrice(BigDecimal regularPrice) {
        Objects.requireNonNull(regularPrice, "regularPrice cannot be null");
        if (regularPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw  new IllegalArgumentException("Regular price cannot be less than zero");
        }

        if (this.salePrice == null) {
            this.salePrice = regularPrice;
        } else if (regularPrice.compareTo(this.salePrice) <= 0) {
            throw new DomainException("Sale Price cannot be greater than regular price");
        }

        this.regularPrice = regularPrice;
    }

    public void setSalePrice(BigDecimal salePrice) {
        Objects.requireNonNull(salePrice, "salePrice cannot be null");
        if (salePrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw  new IllegalArgumentException("Sale price cannot be less than zero");
        }

        if (this.regularPrice == null) {
            this.regularPrice = salePrice;
        } else if (this.regularPrice.compareTo(salePrice) <= 0) {
            throw new DomainException("Sale Price cannot be greater than regular price");
        }

        this.salePrice = salePrice;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void disable() {
        setEnabled(false);
    }

    public void enable() {
        setEnabled(true);
    }

    private void setId(UUID id) {
        Objects.requireNonNull(id, "id cannot be null");
        this.id = id;
    }

    private void setQuantityInStock(Integer quantityInStock) {
        Objects.requireNonNull(quantityInStock, "quantityInStock cannot be null");
        if (quantityInStock < 0) {
            throw  new DomainException("quantityInStock cannot be less than zero");
        }
        this.quantityInStock = quantityInStock;
    }

    public boolean isInStock() {
        return this.quantityInStock != null && this.quantityInStock > 0;
    }

}
