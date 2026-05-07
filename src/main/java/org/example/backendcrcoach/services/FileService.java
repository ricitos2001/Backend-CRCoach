package org.example.backendcrcoach.services;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.ArrayList;

import org.example.backendcrcoach.web.exceptions.ResourceNotFoundException;

@Service
public class FileService {

    private static final List<String> ALLOWED_CONTENT_TYPES = List.of("image/jpeg", "image/png", "image/gif", "image/webp");
    private static final long MAX_FILE_SIZE = 2L * 1024L * 1024L; // 2 MB

    private final Path basePath = Paths.get("uploads/usuario").toAbsolutePath().normalize();
    private final Logger log = LoggerFactory.getLogger(FileService.class);

    public FileService() throws IOException {
        Files.createDirectories(basePath);
    }

    public String guardarFichero(Long usuarioId, MultipartFile fichero) throws IOException {
        validarTipoDeFichero(fichero);
        validarTamanoFichero(fichero);
        String originalFilename = fichero.getOriginalFilename();
        String filename = (originalFilename == null || originalFilename.isBlank()) ? "archivo_por_defecto" : sanitizeFilename(originalFilename);
        Path userDir = basePath.resolve(String.valueOf(usuarioId)).normalize();
        Files.createDirectories(userDir);
        Path rutaFichero = userDir.resolve(filename).normalize();
        Files.copy(fichero.getInputStream(), rutaFichero, StandardCopyOption.REPLACE_EXISTING);
        // Guardamos únicamente la ruta relativa a basePath para evitar inconsistencias
        return basePath.relativize(rutaFichero).toString().replace("\\", "/");
    }

    public Resource cargarFichero(String ruta) {
        try {
            if (ruta == null || ruta.isBlank()) {
                throw new ResourceNotFoundException("Ruta de fichero nula o vacía");
            }

            // Soporte para data URLs (avatar almacenado en la BD como data:image/...;base64,...)
            if (ruta.startsWith("data:")) {
                // Formato: data:[<mediatype>][;base64],<data>
                int comma = ruta.indexOf(',');
                if (comma <= 0) throw new ResourceNotFoundException("Data URL inválida para el avatar");
                String meta = ruta.substring(5, comma); // after 'data:' up to comma
                String dataPart = ruta.substring(comma + 1);
                byte[] bytes;
                if (meta.endsWith(";base64") || meta.contains(";base64")) {
                    bytes = java.util.Base64.getDecoder().decode(dataPart);
                } else {
                    // URL-encoded data
                    bytes = java.net.URLDecoder.decode(dataPart, java.nio.charset.StandardCharsets.UTF_8).getBytes(java.nio.charset.StandardCharsets.UTF_8);
                }
                // Devolvemos como ByteArrayResource (implementa Resource)
                return new org.springframework.core.io.ByteArrayResource(bytes) {
                    @Override
                    public String getFilename() {
                        // intentamos deducir extensión del mediatype
                        String mt = meta.split(";")[0];
                        String ext = "";
                        if (mt.contains("/")) ext = mt.substring(mt.indexOf('/') + 1);
                        return "avatar." + (ext.isEmpty() ? "bin" : ext);
                    }
                };
            }

            // Probamos varias opciones para localizar el fichero (compatibilidad con rutas antiguas)
            List<Path> candidates = new ArrayList<>();

            // 1) Interpretar la ruta tal cual (relativa al working dir o absoluta)
            candidates.add(Paths.get(ruta));
            // 2) Interpretar la ruta relativa al basePath
            candidates.add(basePath.resolve(ruta));

            // 3) También probamos resolviendo contra el padre de basePath (por si la ruta almacenada incluye 'uploads/usuario' completa)
            if (basePath.getParent() != null) candidates.add(basePath.getParent().resolve(ruta));

            Path ficheroPath = null;
            List<String> checked = new ArrayList<>();
            for (Path candidate : candidates) {
                Path normalized = candidate.toAbsolutePath().normalize();
                checked.add(normalized.toString());
                if (Files.exists(normalized)) {
                    ficheroPath = normalized;
                    break;
                }
            }

            if (ficheroPath == null) {
                StringBuilder sb = new StringBuilder();
                sb.append("El fichero no existe: ").append(ruta).append(". Rutas comprobadas: ");
                for (String c : checked) sb.append(c).append("; ");
                throw new ResourceNotFoundException(sb.toString());
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