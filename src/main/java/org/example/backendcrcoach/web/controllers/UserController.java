package org.example.backendcrcoach.web.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.example.backendcrcoach.domain.dto.UserRequestDTO;
import org.example.backendcrcoach.domain.dto.UserResponseDTO;
import org.example.backendcrcoach.domain.entities.User;
import org.example.backendcrcoach.services.UserService;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/v1/users", produces = MediaType.APPLICATION_JSON_VALUE)

public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @Operation(summary = "Listar usuarios", description = "Obtiene una lista paginada de todos los usuarios.", parameters = {@Parameter(name = "page", description = "Número de página (0-indexado)."), @Parameter(name = "size", description = "Tamaño de la página."), @Parameter(name = "sort", description = "Criterios de ordenación en el formato: propiedad,(asc|desc).")})
    public ResponseEntity<Page<UserResponseDTO>> list(Pageable pageable) {
        Page<UserResponseDTO> users = userService.list(pageable);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/id/{id}")
    @Operation(summary = "Obtener usuario por ID", description = "Obtiene los detalles de un usuario específico utilizando su ID.", parameters = {@Parameter(name = "id", description = "ID del usuario a obtener.")})
    public ResponseEntity<UserResponseDTO> getById(@PathVariable(name = "id") Long id) {
        UserResponseDTO user = userService.showById(id);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/username/{username}")
    @Operation(summary = "Obtener usuario por nombre de usuario", description = "Obtiene los detalles de un usuario específico utilizando su nombre de usuario.", parameters = {@Parameter(name = "username", description = "Nombre de usuario del usuario a obtener.")})
    public ResponseEntity<UserResponseDTO> getByName(@PathVariable(name = "username") String username) {
        UserResponseDTO user = userService.showByName(username);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "Obtener usuario por correo electrónico", description = "Obtiene los detalles de un usuario específico utilizando su correo electrónico.", parameters = {@Parameter(name = "email", description = "Correo electrónico del usuario a obtener.")})
    public ResponseEntity<UserResponseDTO> getByEmail(@PathVariable(name = "email") String email) {
        UserResponseDTO user = userService.showByEmail(email);
        return ResponseEntity.ok(user);
    }

    @PostMapping
    @Operation(summary = "Crear usuario", description = "Crea un nuevo usuario con los datos proporcionados.", parameters = {@Parameter(name = "dto", description = "Datos del usuario a crear.")})
    public ResponseEntity<UserResponseDTO> create(@RequestBody @Valid UserRequestDTO dto) {
        UserResponseDTO saved = userService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar usuario", description = "Actualiza los datos de un usuario existente utilizando su ID.", parameters = {@Parameter(name = "id", description = "ID del usuario a actualizar.")})
    public ResponseEntity<UserResponseDTO> update(@PathVariable(name = "id") Long id, @RequestBody @Valid UserRequestDTO dto) {
        UserResponseDTO toggled = userService.update(id, dto);
        return ResponseEntity.ok(toggled);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar usuario", description = "Elimina un usuario existente utilizando su ID.", parameters = {@Parameter(name = "id", description = "ID del usuario a eliminar.")})
    public ResponseEntity<Void> delete(@PathVariable(name = "id") Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/email-exists")
    @Operation(summary = "Verificar existencia de correo electrónico", description = "Verifica si un correo electrónico ya está registrado en el sistema.", parameters = {@Parameter(name = "email", description = "Correo electrónico a verificar.")})
    public ResponseEntity<Map<String, Boolean>> emailExists(@RequestParam String email) {
        boolean exists = userService.existsByEmail(email);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    @GetMapping("/username-exists")
    @Operation(summary = "Verificar existencia de nombre de usuario", description = "Verifica si un nombre de usuario ya está registrado en el sistema.", parameters = {@Parameter(name = "username", description = "Nombre de usuario a verificar.")})
    public ResponseEntity<Map<String, Boolean>> usernameExists(@RequestParam String username) {
        boolean exists = userService.existsByUsername(username);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Obtener perfil del usuario autenticado", description = "Obtiene los detalles del perfil del usuario actualmente autenticado.")
    public ResponseEntity<User> obtenerMiPerfil() {
        User usuario = userService.obtenerMiPerfil();
        return ResponseEntity.ok(usuario);
    }

    @PostMapping("/{id}/avatar")
    @Operation(summary = "Cargar avatar de usuario", description = "Carga o actualiza el avatar de un usuario específico utilizando su ID.", parameters = {@Parameter(name = "id", description = "ID del usuario cuyo avatar se va a cargar.")})
    public ResponseEntity<?> cargarAvatar(@PathVariable(name = "id") Long id, @RequestParam("file") MultipartFile file) {
        try {
            userService.guardarAvatar(id, file);
            return ResponseEntity.ok("Avatar actualizado correctamente.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error al cargar el avatar: " + e.getMessage());
        }
    }

    @GetMapping("/me/avatar")
    @Operation(summary = "Obtener avatar del usuario autenticado", description = "Obtiene el avatar del usuario actualmente autenticado.")
    public ResponseEntity<Resource> obtenerAvatarUsuarioLogueado() {
        Resource avatar = userService.obtenerAvatarGenerico(null);
        try {
            Path avatarPath = Paths.get(avatar.getURL().getPath());
            MediaType mediaType = determinarMediaType(avatarPath);
            return ResponseEntity.ok()
                    .header("Content-Disposition", "inline; filename=\"" + avatar.getFilename() + "\"")
                    .contentType(mediaType)
                    .body(avatar);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al procesar el archivo de avatar", e);
        }
    }

    @GetMapping("/{id}/avatar")
    @Operation(summary = "Obtener avatar de usuario por ID", description = "Obtiene el avatar de un usuario específico utilizando su ID.", parameters = {@Parameter(name = "id", description = "ID del usuario cuyo avatar se va a obtener.")})
    public ResponseEntity<Resource> obtenerAvatar(@PathVariable(name = "id") Long id) {
        Resource avatar = userService.obtenerAvatarGenerico(id);
        try {
            Path avatarPath = Paths.get(avatar.getURL().getPath());
            MediaType mediaType = determinarMediaType(avatarPath);
            return ResponseEntity.ok()
                    .header("Content-Disposition", "inline; filename=\"" + avatar.getFilename() + "\"")
                    .contentType(mediaType)
                    .body(avatar);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al procesar el archivo de avatar", e);
        }
    }

    @Operation(summary = "Determinar MediaType de un archivo", description = "Determina el MediaType de un archivo dado su Path.", parameters = {@Parameter(name = "ficheroPath", description = "Path del archivo cuyo MediaType se va a determinar.")})
    private MediaType determinarMediaType(Path ficheroPath) {
        try {
            String contentType = Files.probeContentType(ficheroPath);
            if (contentType == null || contentType.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El tipo de contenido no puede ser nulo o vacío.");
            }
            return switch (contentType.toLowerCase()) {
                case "image/jpeg" -> MediaType.IMAGE_JPEG;
                case "image/png" -> MediaType.IMAGE_PNG;
                case "image/gif" -> MediaType.IMAGE_GIF;
                default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Formato de imagen no soportado: " + contentType);
            };
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al determinar el tipo de contenido", e);
        }
    }
}
