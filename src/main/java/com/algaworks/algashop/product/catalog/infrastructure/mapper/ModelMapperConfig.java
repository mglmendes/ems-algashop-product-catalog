package com.algaworks.algashop.product.catalog.infrastructure.mapper;


import com.algaworks.algashop.product.catalog.application.product.output.ProductDetailOutput;
import com.algaworks.algashop.product.catalog.application.product.output.ProductSummaryOutput;
import com.algaworks.algashop.product.catalog.application.utility.Mapper;
import com.algaworks.algashop.product.catalog.domain.model.product.Product;
import com.algaworks.algashop.product.catalog.infrastructure.utility.Slugfier;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.modelmapper.convention.NamingConventions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {

    private final Converter<String, String> fromStringToSlugConverter = context -> {
        return Slugfier.slugify(context.getSource());
    };

    private final Converter<String, String> fromStringToShortStringConverter = context -> {
        return StringUtils.abbreviate(context.getSource(), 50);
    };


    @Bean
    public Mapper mapper() {
        ModelMapper modelMapper = new ModelMapper();
        configuration(modelMapper);
        return modelMapper::map;
    }

    private void configuration(ModelMapper modelMapper) {
        modelMapper.getConfiguration()
                .setSourceNamingConvention(NamingConventions.NONE)
                .setDestinationNamingConvention(NamingConventions.NONE)
                .setMatchingStrategy(MatchingStrategies.STRICT);

        modelMapper.createTypeMap(Product.class, ProductDetailOutput.class).addMappings(
                mapping -> {
                    mapping.using(fromStringToSlugConverter).map(Product::getName, ProductDetailOutput::setSlug);
                }
        );
    }

}
