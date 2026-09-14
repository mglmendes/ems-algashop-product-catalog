package com.algaworks.algashop.product.catalog.infrastructure.storage.s3;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Data
@Configuration
@ConfigurationProperties("algashop.storage.s3")
@Validated
public class StorageProviderAwsS3Properties {

    @NotBlank
    private String bucketName;

}
