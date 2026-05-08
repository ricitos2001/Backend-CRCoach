package org.example.backendcrcoach.services;

import org.example.backendcrcoach.web.exceptions.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
public class FileService {

    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp",
            "image/jpg",
            "image/svg",
            "image/xml"
    );

    private static final long MAX_FILE_SIZE = 2L * 1024L * 1024L;

    private final Path basePath;
    private final Logger log = LoggerFactory.getLogger(FileService.class);

    public FileService() throws IOException {

        this.basePath = Paths.get("uploads/usuario")
                .toAbsolutePath()
                .normalize();

        Files.createDirectories(basePath);

        log.info("Base path uploads: {}", basePath);
    }

    public String guardarFichero(Long usuarioId, MultipartFile fichero) throws IOException {

        validarTipoDeFichero(fichero);
        validarTamanoFichero(fichero);

        String originalFilename = fichero.getOriginalFilename();

        String filename =
                (originalFilename == null || originalFilename.isBlank())
                        ? "archivo_por_defecto"
                        : sanitizeFilename(originalFilename);

        Path userDir = basePath.resolve(String.valueOf(usuarioId)).normalize();

        Files.createDirectories(userDir);

        Path rutaFichero = userDir.resolve(filename).normalize();

        Files.copy(
                fichero.getInputStream(),
                rutaFichero,
                StandardCopyOption.REPLACE_EXISTING
        );

        // Guardar SOLO la ruta relativa
        return usuarioId + "/" + filename;
    }

    public Resource cargarFichero(String ruta) {

        try {

            if (ruta == null || ruta.isBlank()) {
                throw new ResourceNotFoundException("Ruta de fichero vacía");
            }

            /*
             * =========================================================
             * DATA URL
             * =========================================================
             */
            if (ruta.startsWith("data:")) {

                int comma = ruta.indexOf(',');

                if (comma <= 0) {
                    throw new ResourceNotFoundException("Data URL inválida");
                }

                String meta = ruta.substring(5, comma);
                String dataPart = ruta.substring(comma + 1);

                byte[] bytes;

                if (meta.contains(";base64")) {
                    bytes = Base64.getDecoder().decode(dataPart);
                } else {
                    bytes = URLDecoder.decode(
                            dataPart,
                            StandardCharsets.UTF_8
                    ).getBytes(StandardCharsets.UTF_8);
                }

                return new ByteArrayResource(bytes) {

                    @Override
                    public String getFilename() {

                        String mt = meta.split(";")[0];

                        String ext = "bin";

                        if (mt.contains("/")) {
                            ext = mt.substring(mt.indexOf('/') + 1);
                        }

                        return "avatar." + ext;
                    }
                };
            }

            /*
             * =========================================================
             * PATH NORMAL
             * =========================================================
             */

            String rutaNormalizada = ruta.replace("\\", "/");

            // Evitar path traversal
            if (rutaNormalizada.contains("..")) {
                throw new ResourceNotFoundException("Ruta inválida");
            }

            List<Path> candidates = new ArrayList<>();

            Path inputPath = Paths.get(rutaNormalizada);

            // Ruta absoluta
            if (inputPath.isAbsolute()) {
                candidates.add(inputPath.normalize());
            }

            // Ruta relativa respecto a uploads/usuario
            candidates.add(basePath.resolve(rutaNormalizada).normalize());

            // Compatibilidad con rutas antiguas
            candidates.add(
                    Paths.get(rutaNormalizada)
                            .toAbsolutePath()
                            .normalize()
            );

            Path ficheroPath = null;

            List<String> checked = new ArrayList<>();

            for (Path candidate : candidates) {

                checked.add(candidate.toString());

                if (Files.exists(candidate) && Files.isRegularFile(candidate)) {

                    ficheroPath = candidate;
                    break;
                }
            }

            if (ficheroPath == null) {

                throw new ResourceNotFoundException(
                        "El fichero no existe: "
                                + ruta
                                + ". Rutas comprobadas: "
                                + String.join("; ", checked)
                );
            }

            return new UrlResource(ficheroPath.toUri());

        } catch (IOException e) {

            throw new RuntimeException(
                    "Error al cargar el fichero: " + ruta,
                    e
            );
        }
    }

    private void validarTipoDeFichero(MultipartFile fichero) {

        String contentType = fichero.getContentType();

        if (contentType == null ||
                !ALLOWED_CONTENT_TYPES.contains(contentType)) {

            throw new IllegalArgumentException(
                    "Formato no permitido"
            );
        }
    }

    private void validarTamanoFichero(MultipartFile fichero) {

        if (fichero.getSize() > MAX_FILE_SIZE) {

            throw new IllegalArgumentException(
                    "Tamaño máximo permitido: 2MB"
            );
        }
    }

    private String sanitizeFilename(String filename) {

        return Paths.get(filename)
                .getFileName()
                .toString()
                .replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}