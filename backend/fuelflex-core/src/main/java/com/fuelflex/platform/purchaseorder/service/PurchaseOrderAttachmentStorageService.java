package com.fuelflex.platform.purchaseorder.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class PurchaseOrderAttachmentStorageService {
    public static final long MAX_FILE_SIZE = 2 * 1024 * 1024;
    private static final Set<String> TYPES = Set.of("application/pdf", "image/jpeg", "image/png");
    private final Path root;
    public PurchaseOrderAttachmentStorageService(@Value("${fuelflex.storage.purchase-orders-directory:uploads/purchase-orders}") String directory) { root = Path.of(directory).toAbsolutePath().normalize(); }
    public String store(UUID organizationId, UUID orderId, MultipartFile file) {
        validate(file);
        String type = file.getContentType().toLowerCase(Locale.ROOT);
        String extension = type.equals("application/pdf") ? ".pdf" : type.equals("image/png") ? ".png" : ".jpg";
        String key = organizationId + "/" + orderId + "/" + UUID.randomUUID() + extension;
        Path destination = root.resolve(key).normalize();
        verify(destination);
        try { Files.createDirectories(destination.getParent()); try (InputStream input = file.getInputStream()) { Files.copy(input, destination, StandardCopyOption.REPLACE_EXISTING); } return key; }
        catch (IOException ex) { throw new IllegalStateException("Impossible d’enregistrer la pièce jointe", ex); }
    }
    public byte[] read(String key) {
        Path path = root.resolve(key).normalize(); verify(path);
        try { return Files.readAllBytes(path); } catch (IOException ex) { throw new IllegalStateException("Pièce jointe introuvable", ex); }
    }
    public void delete(String key) { Path path = root.resolve(key).normalize(); verify(path); try { Files.deleteIfExists(path); } catch (IOException ex) { throw new IllegalStateException("Impossible de supprimer la pièce jointe", ex); } }
    private void validate(MultipartFile file) { if (file == null || file.isEmpty()) throw new IllegalArgumentException("Le fichier est obligatoire"); if (file.getSize() > MAX_FILE_SIZE) throw new IllegalArgumentException("La pièce jointe ne doit pas dépasser 2 Mo"); String type = file.getContentType(); if (type == null || !TYPES.contains(type.toLowerCase(Locale.ROOT))) throw new IllegalArgumentException("Format accepté : PDF, JPG ou PNG"); }
    private void verify(Path path) { if (!path.startsWith(root)) throw new IllegalArgumentException("Chemin de stockage non autorisé"); }
}
