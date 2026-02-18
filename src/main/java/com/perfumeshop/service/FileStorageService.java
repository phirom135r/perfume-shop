package com.perfumeshop.service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class FileStorageService {

    // save into: src/main/resources/static/uploads/products
    private final Path root = Paths.get("src/main/resources/static/uploads/products");

    public String saveImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) return null;

        Files.createDirectories(root);

        String original = StringUtils.cleanPath(file.getOriginalFilename());
        String ext = "";

        int dot = original.lastIndexOf('.');
        if (dot >= 0) ext = original.substring(dot).toLowerCase();

        String newName = UUID.randomUUID().toString().replace("-", "") + ext;

        Path target = root.resolve(newName);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        return newName;
    }

    public void deleteIfExists(String filename) {
        if (filename == null || filename.isBlank()) return;
        try {
            Path p = root.resolve(filename);
            Files.deleteIfExists(p);
        } catch (Exception ignored) {}
    }
}