package com.algawors.algashop.product.catalog.application.product.query;

import com.algawors.algashop.product.catalog.application.product.output.ProductDetailOutput;
import com.algawors.algashop.product.catalog.presentation.model.PageModel;
import org.springframework.stereotype.Service;

import java.util.UUID;

public interface ProductQueryService {

   PageModel<ProductDetailOutput> filter(Integer size, Integer number);
   ProductDetailOutput findById(UUID productId);
}
