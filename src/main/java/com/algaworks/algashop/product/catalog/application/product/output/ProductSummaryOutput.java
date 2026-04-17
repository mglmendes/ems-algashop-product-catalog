package com.algaworks.algashop.product.catalog.application.product.output;

import com.algaworks.algashop.product.catalog.infrastructure.utility.Slugfier;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductSummaryOutput {

    private UUID id;
    private OffsetDateTime addedAt;
    private String name;
    private String brand;
    private BigDecimal regularPrice;
    private BigDecimal salePrice;
    private Boolean inStock;
    private Boolean enabled;
    private String shortDescription;
    private CategoryMinimalOutput category;

    private String slug;
    private Boolean hasDiscount;

    private Integer quantityInStock;
    private Integer discountPercentageRounded;

    private Float score;

    public String getSlug() {
        return Slugfier.slugify(this.name);
    }
}
