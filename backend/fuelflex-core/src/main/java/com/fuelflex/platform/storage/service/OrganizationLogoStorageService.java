package com.fuelflex.platform.storage.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class OrganizationLogoStorageService {

    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024;

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/png",
            "image/jpeg",
            "image/webp",
            "image/svg+xml"
    );

    private final Path storageRoot;
    private final String publicUrlPrefix;

    public OrganizationLogoStorageService(
            @Value(
                    "${fuelflex.storage.organization-logos-directory}"
            )
            String storageDirectory,
            @Value(
                    "${fuelflex.storage.organization-logos-url-prefix}"
            )
            String publicUrlPrefix
    ) {
        this.storageRoot = Path.of(storageDirectory)
                .toAbsolutePath()
                .normalize();

        this.publicUrlPrefix =
                normalizeUrlPrefix(publicUrlPrefix);
    }

    public String store(
            UUID organizationId,
            MultipartFile file
    ) {
        validateOrganizationId(organizationId);
        validateFile(file);

        String extension = resolveExtension(file);
        String fileName =
                "logo-" + UUID.randomUUID() + extension;

        Path organizationDirectory =
                storageRoot.resolve(organizationId.toString())
                        .normalize();

        verifyPathIsInsideStorageRoot(
                organizationDirectory
        );

        try {
            Files.createDirectories(organizationDirectory);

            Path destination =
                    organizationDirectory.resolve(fileName)
                            .normalize();

            verifyPathIsInsideStorageRoot(destination);

            try (InputStream inputStream =
                         file.getInputStream()) {

                Files.copy(
                        inputStream,
                        destination,
                        StandardCopyOption.REPLACE_EXISTING
                );
            }

            return publicUrlPrefix
                    + "/"
                    + organizationId
                    + "/"
                    + fileName;

        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Impossible d’enregistrer le logo de l’organisation",
                    exception
            );
        }
    }

    public void deleteByPublicUrl(String logoUrl) {
        if (logoUrl == null || logoUrl.isBlank()) {
            return;
        }

        String normalizedLogoUrl = logoUrl.trim();

        if (!normalizedLogoUrl.startsWith(
                publicUrlPrefix + "/"
        )) {
            return;
        }

        String relativePath = normalizedLogoUrl
                .substring(publicUrlPrefix.length())
                .replaceFirst("^/+", "");

        if (relativePath.isBlank()) {
            return;
        }

        Path filePath = storageRoot
                .resolve(relativePath)
                .normalize();

        verifyPathIsInsideStorageRoot(filePath);

        try {
            Files.deleteIfExists(filePath);
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Impossible de supprimer l’ancien logo",
                    exception
            );
        }
    }

    private void validateOrganizationId(
            UUID organizationId
    ) {
        if (organizationId == null) {
            throw new IllegalArgumentException(
                    "L’identifiant de l’organisation est obligatoire"
            );
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "Le fichier du logo est obligatoire"
            );
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                    "Le logo ne doit pas dépasser 2 Mo"
            );
        }

        String contentType = file.getContentType();

        if (contentType == null
                || !ALLOWED_CONTENT_TYPES.contains(
                        contentType.toLowerCase(Locale.ROOT)
                )) {

            throw new IllegalArgumentException(
                    "Format de logo non autorisé. Formats acceptés : PNG, JPG, WEBP et SVG"
            );
        }
    }

    private String resolveExtension(
            MultipartFile file
    ) {
        String contentType = file.getContentType();

        if (contentType == null) {
            throw new IllegalArgumentException(
                    "Le type du fichier est introuvable"
            );
        }

        return switch (
                contentType.toLowerCase(Locale.ROOT)
        ) {
            case "image/png" -> ".png";
            case "image/jpeg" -> ".jpg";
            case "image/webp" -> ".webp";
            case "image/svg+xml" -> ".svg";
            default -> throw new IllegalArgumentException(
                    "Format du logo non pris en charge"
            );
        };
    }

    private void verifyPathIsInsideStorageRoot(
            Path path
    ) {
        if (!path.startsWith(storageRoot)) {
            throw new IllegalArgumentException(
                    "Chemin de stockage non autorisé"
            );
        }
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