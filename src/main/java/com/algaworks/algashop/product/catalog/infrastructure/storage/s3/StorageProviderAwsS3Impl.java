package com.algaworks.algashop.product.catalog.infrastructure.storage.s3;

import com.algaworks.algashop.product.catalog.application.storage.FileReference;
import com.algaworks.algashop.product.catalog.application.storage.StorageProvider;
import io.awspring.cloud.s3.ObjectMetadata;
import io.awspring.cloud.s3.S3Exception;
import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.net.URL;

@Component
@RequiredArgsConstructor
public class StorageProviderAwsS3Impl implements StorageProvider {

    private final StorageProviderAwsS3Properties properties;
    private final S3Template s3Template;

    @Override
    public boolean healthCheck() {
        try {
            return s3Template.bucketExists(properties.getBucketName());
        } catch (Exception e) {
            return false;
        }
    }

    @SneakyThrows
    @Override
    public URL requestUploadUrl(FileReference fileReference) {
        String bucketName = properties.getBucketName();
        String key = fileReference.getFileName();
        if (fileExists(key)) {
            throw new StorageProviderException("Remote file %s already exists".formatted(key));
        }

        ObjectMetadata.Builder metadataBuilder = new ObjectMetadata.Builder();

        if (fileReference.getAllowPublicRead()) {
            metadataBuilder.acl("public-read");
        }

        try {
            return s3Template.createSignedPutURL(
                    bucketName,
                    key,
                    fileReference.getExpiresIn(),
                    metadataBuilder.build(),
                    fileReference.getContentType().toString()
            );
        } catch (S3Exception e) {
            throw new StorageProviderException(
                    "Unknow error when tried to create signed url for file %s".formatted(key),e);
        }

    }

    @Override
    public void deleteFile(String remoteFileName) {

    }

    @Override
    public boolean fileExists(String remoteFileName) {
        return s3Template.objectExists(properties.getBucketName(), remoteFileName);
    }
}
