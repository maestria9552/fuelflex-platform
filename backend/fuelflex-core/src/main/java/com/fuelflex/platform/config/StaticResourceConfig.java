package com.fuelflex.platform.config;

import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    private final Path organizationLogosDirectory;
    private final String organizationLogosUrlPrefix;

    public StaticResourceConfig(
            @Value(
                    "${fuelflex.storage.organization-logos-directory}"
            )
            String organizationLogosDirectory,
            @Value(
                    "${fuelflex.storage.organization-logos-url-prefix}"
            )
            String organizationLogosUrlPrefix
    ) {
        this.organizationLogosDirectory =
                Path.of(organizationLogosDirectory)
                        .toAbsolutePath()
                        .normalize();

        this.organizationLogosUrlPrefix =
                normalizeUrlPrefix(
                        organizationLogosUrlPrefix
                );
    }

    @Override
    public void addResourceHandlers(
            ResourceHandlerRegistry registry
    ) {
        String resourceLocation =
                organizationLogosDirectory
                        .toUri()
                        .toString();

        registry.addResourceHandler(
                        organizationLogosUrlPrefix + "/**"
                )
                .addResourceLocations(resourceLocation);
    }

    private String normalizeUrlPrefix(
            String value
    ) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "Le préfixe public des logos est obligatoire"
            );
        }

        String normalizedValue = value.trim();

        if (!normalizedValue.startsWith("/")) {
            normalizedValue = "/" + normalizedValue;
        }

        return normalizedValue.replaceAll("/+$", "");
    }
}