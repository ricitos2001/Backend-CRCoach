package org.example.backendcrcoach.services;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;

@Service
public class FileService {

    private static final List<String> ALLOWED_CONTENT_TYPES = List.of("image/jpeg", "image/png", "image/gif", "image/webp");
    private static final long MAX_FILE_SIZE = 2L * 1024L * 1024L; // 2 MB

    private final Path basePath = Paths.get("uploads/usuario");

    public FileService() throws IOException {
        Files.createDirectories(basePath);
    }

    public String guardarFichero(Long usuarioId, MultipartFile fichero) throws IOException {
        validarTipoDeFichero(fichero);
        validarTamanoFichero(fichero);
        String originalFilename = fichero.getOriginalFilename();
        String filename = (originalFilename == null || originalFilename.isBlank()) ? "archivo_por_defecto" : sanitizeFilename(originalFilename);
        Path userDir = basePath.resolve(String.valueOf(usuarioId));
        Files.createDirectories(userDir);
        Path rutaFichero = userDir.resolve(filename);
        Files.copy(fichero.getInputStream(), rutaFichero, StandardCopyOption.REPLACE_EXISTING);
        return rutaFichero.toString();
    }

    public Resource cargarFichero(String ruta) {
        try {
            Path ficheroPath = Paths.get(ruta);
            if (!Files.exists(ficheroPath)) {
                throw new NoSuchFileException("El fichero no existe: " + ruta);
            }
            return new UrlResource(ficheroPath.toUri());
        } catch (IOException e) {
            throw new RuntimeException("Error al cargar el fichero: " + ruta, e);
        }
    }

    private void validarTipoDeFichero(MultipartFile fichero) {
        String contentType = fichero.getContentType();
        if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Formato de fichero no permitido. Solo JPG, PNG, GIF, y WEBP.");
        }
    }

    private void validarTamanoFichero(MultipartFile fichero) {
        if (fichero.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("El fichero es demasiado grande. Tamaño máximo permitido: 2 MB.");
        }
    }

    private String sanitizeFilename(String filename) {
        // eliminar rutas y caracteres problemáticos
        String name = Paths.get(filename).getFileName().toString();
        return name.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}