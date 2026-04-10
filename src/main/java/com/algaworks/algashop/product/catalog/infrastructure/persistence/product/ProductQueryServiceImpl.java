package com.algaworks.algashop.product.catalog.infrastructure.persistence.product;

import com.algaworks.algashop.product.catalog.application.product.output.ProductDetailOutput;
import com.algaworks.algashop.product.catalog.application.product.output.ProductSummaryOutput;
import com.algaworks.algashop.product.catalog.application.product.service.query.ProductFilter;
import com.algaworks.algashop.product.catalog.application.product.service.query.ProductQueryService;
import com.algaworks.algashop.product.catalog.application.utility.Mapper;
import com.algaworks.algashop.product.catalog.domain.model.product.Product;
import com.algaworks.algashop.product.catalog.domain.model.product.ProductNotFoundException;
import com.algaworks.algashop.product.catalog.domain.model.product.ProductRepository;
import com.algaworks.algashop.product.catalog.presentation.model.PageModel;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductQueryServiceImpl implements ProductQueryService {

    private final ProductRepository productRepository;
    private final Mapper mapper;
    private final MongoOperations mongoOperations;

    @Override
    public PageModel<ProductSummaryOutput> filter(ProductFilter productFilter) {
        Query query = queryWith(productFilter);

        long totalItems = mongoOperations.count(query, Product.class);

        Sort sort = sortWith(productFilter);

        PageRequest pageRequest = PageRequest.of(productFilter.getPage(), productFilter.getSize(), sort);
        Query pagedQuery = query.with(pageRequest);
        List<Product> products;
        int totalPages = 0;
        if (totalItems > 0) {
            products = mongoOperations.find(pagedQuery, Product.class);
            totalPages = (int) Math.ceil((double) totalItems / (double) products.size());
        } else {
            products = new ArrayList<>();
        }

        List<ProductSummaryOutput> productSummaries =
                products.stream().map(p -> mapper.convert(p, ProductSummaryOutput.class)).toList();
        return PageModel.<ProductSummaryOutput>builder()
                .content(productSummaries)
                .number(pageRequest.getPageNumber())
                .size(pageRequest.getPageSize())
                .totalElements(totalItems)
                .totalPages(totalPages)
                .build();
    }

    private Sort sortWith(ProductFilter productFilter) {
        return Sort.by(productFilter.getSortDirectionOrDefault(),
                productFilter.getSortByPropertyOrDefault().getPropertyName());
    }

    private Query queryWith(ProductFilter filter) {
        Query query = new Query();
        if (filter.getEnabled() != null) {
            query.addCriteria(Criteria.where("enabled").is(filter.getEnabled()));
        }

        if (filter.getAddedAtFrom() != null && filter.getAddedAtTo() != null) {
            query.addCriteria(Criteria.where("addedAt").gte(filter.getAddedAtFrom()).lte(filter.getAddedAtTo()));
        } else {
            if (filter.getAddedAtFrom() != null) {
                query.addCriteria(Criteria.where("addedAt").gte(filter.getAddedAtFrom()));
            } else if (filter.getAddedAtTo() != null) {
                query.addCriteria(Criteria.where("addedAt").lte(filter.getAddedAtTo()));
            }
        }

        if (filter.getPriceFrom() != null && filter.getPriceTo() != null) {
            query.addCriteria(Criteria.where("salePrice").gte(filter.getPriceFrom()).lte(filter.getPriceTo()));
        } else {
            if (filter.getPriceFrom() != null) {
                query.addCriteria(Criteria.where("salePrice").gte(filter.getPriceFrom()));
            } else if (filter.getPriceTo() != null) {
                query.addCriteria(Criteria.where("salePrice").lte(filter.getPriceTo()));
            }
        }

        return query;
    }

    @Override
    public ProductDetailOutput findById(UUID productId) {
        Product product = productRepository.findById(productId).orElseThrow(
                () -> new ProductNotFoundException(productId)
        );
        return mapper.convert(product, ProductDetailOutput.class);
    }
}
