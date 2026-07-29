package com.fuelflex.platform.product.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.fuelflex.platform.product.dto.request.ProductRequest;
import com.fuelflex.platform.product.dto.response.ProductResponse;
import com.fuelflex.platform.product.service.ProductService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/organizations/{organizationId}/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse create(
            @PathVariable UUID organizationId,
            @Valid @RequestBody ProductRequest request
    ) {
        return productService.create(
                organizationId,
                request
        );
    }

    @GetMapping
    public List<ProductResponse> findAll(
            @PathVariable UUID organizationId
    ) {
        return productService.findAllByOrganization(
                organizationId
        );
    }

    @GetMapping("/active")
    public List<ProductResponse> findActive(
            @PathVariable UUID organizationId
    ) {
        return productService.findActiveByOrganization(
                organizationId
        );
    }

    @GetMapping("/category/{categoryId}")
    public List<ProductResponse> findByCategory(
            @PathVariable UUID organizationId,
            @PathVariable UUID categoryId
    ) {
        return productService.findByCategory(
                organizationId,
                categoryId
        );
    }

    @GetMapping("/{productId}")
    public ProductResponse findById(
            @PathVariable UUID organizationId,
            @PathVariable UUID productId
    ) {
        return productService.findById(
                organizationId,
                productId
        );
    }

    @PutMapping("/{productId}")
    public ProductResponse update(
            @PathVariable UUID organizationId,
            @PathVariable UUID productId,
            @Valid @RequestBody ProductRequest request
    ) {
        return productService.update(
                organizationId,
                productId,
                request
        );
    }

    @DeleteMapping("/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable UUID organizationId,
            @PathVariable UUID productId
    ) {
        productService.delete(
                organizationId,
                productId
        );
    }
}