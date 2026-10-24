package com.algaworks.algashop.product.catalog.presentation.controller;

import com.algaworks.algashop.product.catalog.application.upload.input.UploadRequestInput;
import com.algaworks.algashop.product.catalog.application.upload.output.UploadResponseOutput;
import com.algaworks.algashop.product.catalog.application.upload.service.UploadRequestApplicationService;
import com.algaworks.algashop.product.catalog.infrastructure.security.SecurityAnnotations.CanWriteProductsImages;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/upload-requests")
@RequiredArgsConstructor
public class UploadRequestController {

    private final UploadRequestApplicationService uploadRequestApplicationService;

    @CanWriteProductsImages
    @PostMapping
    public UploadResponseOutput requestUpload(@RequestBody @Valid UploadRequestInput uploadRequestInput) {
        return uploadRequestApplicationService.requestPreSignedUrl(uploadRequestInput);
    }
}
