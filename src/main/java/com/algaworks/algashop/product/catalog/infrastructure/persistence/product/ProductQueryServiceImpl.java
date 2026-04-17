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
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.CriteriaDefinition;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.stereotype.Service;

import java.util.*;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Service
@RequiredArgsConstructor
public class ProductQueryServiceImpl implements ProductQueryService {

    private final ProductRepository productRepository;
    private final Mapper mapper;
    private final MongoOperations mongoOperations;

    @Override
    public PageModel<ProductSummaryOutput> filter(ProductFilter productFilter) {
        Optional<Criteria> criteria = buildCriteria(productFilter);
        Optional<TextCriteria> textCriteria = buildTextCriteria(productFilter);

        Query query = new Query();
        textCriteria.ifPresent(query::addCriteria);
        criteria.ifPresent(query::addCriteria);

        long totalElements = mongoOperations.count(query, Product.class);

        if (totalElements == 0) {
            return PageModel.<ProductSummaryOutput>builder()
                    .number(0)
                    .totalElements(0)
                    .size(0)
                    .totalPages(0)
                    .build();
        }

        List<AggregationOperation> operations = new ArrayList<>();

        textCriteria.ifPresent(c -> {
            operations.add(Aggregation.match(c));
            AggregationOperation addTextScoreField = context -> {
                return new Document("$addFields", new Document("score", new Document("$meta", "textScore")));
            };
            operations.add(addTextScoreField);
        });
        criteria.ifPresent(c -> operations.add(Aggregation.match(c)));

        PageRequest pageRequest = PageRequest.of(productFilter.getPage(), productFilter.getSize());
        operations.addAll(Arrays.asList(
                lookup("categories", "categoryId", "_id", "category"),
                unwind("$category"),
                sort(sortWith(productFilter)),
                projectionForSummary(),
                skip(pageRequest.getOffset()),
                limit(productFilter.getSize())

        ));

        Aggregation aggregation = newAggregation(operations);

        List<ProductSummaryOutput> productSummaryOutputs = mongoOperations.aggregate(aggregation, Product.class, ProductSummaryOutput.class).getMappedResults();

        int totalPages = (int) Math.ceil((double) totalElements / (double) productFilter.getSize());

        return PageModel.<ProductSummaryOutput>builder()
                .content(productSummaryOutputs)
                .number(productFilter.getPage())
                .size(productFilter.getSize())
                .totalElements(totalElements)
                .totalPages(totalPages)
                .build();
    }

    private ProjectionOperation projectionForSummary() {
        return project()
                .and("_id").as("_id")
                .and("addedAt").as("addedAt")
                .and("name").as("name")
                .and("brand").as("brand")
                .and("regularPrice").as("regularPrice")
                .and("salePrice").as("salePrice")
                .and("enabled").as("enabled")
                .and("quantityInStock").as("quantityInStock")
                .and("discountPercentageRounded").as("discountPercentageRounded")
                .and("score").as("score")
                .and("category._id").as("category._id")
                .and("category.name").as("category.name")
                .andExpression("salePrice < regularPrice").as("hasDiscount")
                .andExpression("quantityInStock > 0").as("inStock")
                .and(StringOperators.Substr.valueOf("description")
                        .substring(0, 50)).as("shortDescription");

    }

    private Sort sortWith(ProductFilter productFilter) {
        if (StringUtils.isNotBlank(productFilter.getTerm())) {
            return Sort.by("score");
        }
        return Sort.by(productFilter.getSortDirectionOrDefault(),
                productFilter.getSortByPropertyOrDefault().getPropertyName());
    }

    private Optional<Criteria> buildCriteria(ProductFilter filter) {
        List<CriteriaDefinition> criterias = new ArrayList<>();
        if (filter.getEnabled() != null) {
            criterias.add(Criteria.where("enabled").is(filter.getEnabled()));
        }

        if (filter.getAddedAtFrom() != null && filter.getAddedAtTo() != null) {
            criterias.add(Criteria.where("addedAt").gte(filter.getAddedAtFrom()).lte(filter.getAddedAtTo()));
        } else {
            if (filter.getAddedAtFrom() != null) {
                criterias.add(Criteria.where("addedAt").gte(filter.getAddedAtFrom()));
            } else if (filter.getAddedAtTo() != null) {
                criterias.add(Criteria.where("addedAt").lte(filter.getAddedAtTo()));
            }
        }

        if (filter.getPriceFrom() != null && filter.getPriceTo() != null) {
            criterias.add(Criteria.where("salePrice").gte(filter.getPriceFrom()).lte(filter.getPriceTo()));
        } else {
            if (filter.getPriceFrom() != null) {
                criterias.add(Criteria.where("salePrice").gte(filter.getPriceFrom()));
            } else if (filter.getPriceTo() != null) {
                criterias.add(Criteria.where("salePrice").lte(filter.getPriceTo()));
            }
        }

        if (filter.getHasDiscount() != null) {
            if (filter.getHasDiscount()) {
                criterias.add(AggregationExpressionCriteria.whereExpr(
                        ComparisonOperators.valueOf("$salePrice").lessThan("$regularPrice")
                ));
            } else {
                criterias.add(AggregationExpressionCriteria.whereExpr(
                        ComparisonOperators.valueOf("$salePrice").equalTo("$regularPrice")
                ));
            }
        }

        if (filter.getInStock() != null) {
            if (filter.getInStock()) {
                criterias.add(Criteria.where("quantityInStock").gt(0));
            } else {
                criterias.add(Criteria.where("quantityInStock").is(0));
            }
        }

        if (filter.getCategoriesId() != null && filter.getCategoriesId().length > 0) {
            criterias.add(Criteria.where("categoryId").in((Object[])filter.getCategoriesId()));
        }

        if (criterias.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new Criteria().andOperator(criterias.toArray(new Criteria[0])));
    }

    public Optional<TextCriteria> buildTextCriteria(ProductFilter filter) {
        if (StringUtils.isNotBlank(filter.getTerm())) {
            return Optional.of(
                    TextCriteria.forDefaultLanguage().matching(filter.getTerm())
            );
        }
        return Optional.empty();
    }

    @Override
    public ProductDetailOutput findById(UUID productId) {
        Product product = productRepository.findById(productId).orElseThrow(
                () -> new ProductNotFoundException(productId)
        );
        return mapper.convert(product, ProductDetailOutput.class);
    }
}
