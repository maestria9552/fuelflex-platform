package com.fuelflex.platform.product.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fuelflex.platform.product.dto.request.ProductCategoryRequest;
import com.fuelflex.platform.product.dto.response.ProductCategoryResponse;
import com.fuelflex.platform.product.service.ProductCategoryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(
        "/api/v1/organizations/{organizationId}/product-categories"
)
@RequiredArgsConstructor
public class ProductCategoryController {

    private final ProductCategoryService productCategoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductCategoryResponse create(
            @PathVariable UUID organizationId,
            @Valid
            @RequestBody ProductCategoryRequest request
    ) {
        return productCategoryService.create(
                organizationId,
                request
        );
    }

    @GetMapping
    public List<ProductCategoryResponse> findAllByOrganization(
            @PathVariable UUID organizationId
    ) {
        return productCategoryService
                .findAllByOrganization(organizationId);
    }

    @GetMapping("/{categoryId}")
    public ProductCategoryResponse findById(
            @PathVariable UUID organizationId,
            @PathVariable UUID categoryId
    ) {
        return productCategoryService.findById(
                organizationId,
                categoryId
        );
    }

    @PutMapping("/{categoryId}")
    public ProductCategoryResponse update(
            @PathVariable UUID organizationId,
            @PathVariable UUID categoryId,
            @Valid
            @RequestBody ProductCategoryRequest request
    ) {
        return productCategoryService.update(
                organizationId,
                categoryId,
                request
        );
    }

    @DeleteMapping("/{categoryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable UUID organizationId,
            @PathVariable UUID categoryId
    ) {
        productCategoryService.delete(
                organizationId,
                categoryId
        );
    }
}