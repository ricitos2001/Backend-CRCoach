package org.example.backendcrcoach.web.controllers;

import jakarta.validation.Valid;
import org.example.backendcrcoach.domain.dto.SessionRequestDTO;
import org.example.backendcrcoach.domain.dto.SessionResponseDTO;
import org.example.backendcrcoach.services.SessionService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/sessions", produces = MediaType.APPLICATION_JSON_VALUE)
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }
    @Operation(summary = "Listar sesiones", description = "Obtiene una lista paginada de sesiones.")
    @GetMapping
    public ResponseEntity<Page<SessionResponseDTO>> list(Pageable pageable) {
        Page<SessionResponseDTO> session = sessionService.list(pageable);
        return ResponseEntity.ok(session);
    }

    @Operation(summary = "Obtener sesión por ID", description = "Recupera una sesión por su ID.")
    @GetMapping("/id/{id}")
    public ResponseEntity<SessionResponseDTO> getById(@PathVariable(name = "id") Long id) {
        SessionResponseDTO session = sessionService.showById(id);
        return ResponseEntity.ok(session);
    }

    @Operation(summary = "Obtener sesión por título", description = "Recupera una sesión por su título.")
    @GetMapping("/title/{title}")
    public ResponseEntity<SessionResponseDTO> getByName(@PathVariable(name = "title") String title) {
        SessionResponseDTO session = sessionService.showByTitle(title);
        return ResponseEntity.ok(session);
    }

    @Operation(summary = "Crear sesión", description = "Crea una nueva sesión con los datos proporcionados.")
    @PostMapping
    public ResponseEntity<SessionResponseDTO> create(@RequestBody @Valid SessionRequestDTO dto) {
        SessionResponseDTO saved = sessionService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @Operation(summary = "Actualizar sesión", description = "Actualiza una sesión existente por su ID.")
    @PutMapping("/{id}")
    public ResponseEntity<SessionResponseDTO> update(@PathVariable(name = "id") Long id, @RequestBody @Valid SessionRequestDTO dto) {
        SessionResponseDTO toggled = sessionService.update(id, dto);
        return ResponseEntity.ok(toggled);
    }

    @Operation(summary = "Eliminar sesión", description = "Elimina una sesión por su ID.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable(name = "id") Long id) {
        sessionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Listar sesiones por usuario", description = "Obtiene la lista de sesiones asociadas al email del usuario.")
    @GetMapping("/user/{email}")
    public ResponseEntity<List<SessionResponseDTO>> listByUserEmail(@PathVariable @RequestParam(name = "email") String email) {
        List<SessionResponseDTO> sessions = sessionService.listByUserEmail(email);
        return ResponseEntity.ok(sessions);
    }
}
