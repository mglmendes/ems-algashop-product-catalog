package com.algaworks.algashop.product.catalog.infrastructure.listener.product;

import com.algaworks.algashop.product.catalog.domain.model.product.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ProductEventListener {

    @EventListener(ProductPriceChangedEvent.class)
    public void handle(ProductPriceChangedEvent event) {
        log.info("Product {} regular price changed from {} to {} and sale price from {} to {}",
                event.getProductId(),
                event.getOldRegularPrice(),
                event.getNewRegularPrice(),
                event.getOldSalePrice(),
                event.getNewSalePrice());
    }

    @EventListener(ProductPlacedOnSaleEvent.class)
    public void handle(ProductPlacedOnSaleEvent event) {
        log.info("Product {} placed to sale. Regular Price {} and Sale Price {}",
                event.getProductId(),
                event.getRegularPrice(),
                event.getSalePrice());
    }

    @EventListener(ProductAddedEvent.class)
    public void handle(ProductAddedEvent event) {
        log.info("Product {} added to sale.", event.getProductId());
    }

    @EventListener(ProductDelistedEvent.class)
    public void handle(ProductDelistedEvent  event) {
        log.info("Product {} delisted from sale.", event.getProductId());
    }

    @EventListener(ProductListedEvent.class)
    public void handle(ProductListedEvent event) {
        log.info("Product {} listed from sale.", event.getProductId());
    }

    @EventListener(ProductRestockedEvent.class)
    public void handle(ProductRestockedEvent event) {
        log.info("Product {} restocked from sale.", event.getProductId());
    }

    @EventListener(ProductSoldOutEvent.class)
    public void handle(ProductSoldOutEvent event) {
        log.info("Product {} sold out from sale.", event.getProductId());
    }
}
