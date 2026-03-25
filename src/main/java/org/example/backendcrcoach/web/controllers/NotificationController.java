package org.example.backendcrcoach.web.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.example.backendcrcoach.domain.dto.NotificationRequestDTO;
import org.example.backendcrcoach.domain.dto.NotificationResponseDTO;
import org.example.backendcrcoach.services.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/v1/notifications", produces = MediaType.APPLICATION_JSON_VALUE)
public class NotificationController {

    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Listar notificaciones paginadas", description = "Recupera una lista paginada de notificaciones.", parameters = {@Parameter(name = "pageable", description = "Información de paginación")})
    public ResponseEntity<Page<NotificationResponseDTO>> getNotifications(Pageable pageable) {
        Page<NotificationResponseDTO> notifications = service.findAll(pageable);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/myNotifications/{email}")
    @Operation(summary = "Listar notificaciones paginadas por correo", description = "Recupera una lista paginada de notificaciones asociadas a un correo de usuario específico.", parameters = {@Parameter(name = "email", description = "Correo electrónico del usuario"), @Parameter(name = "pageable", description = "Información de paginación")})
    public ResponseEntity<Page<NotificationResponseDTO>> getNotificationsByUserEmail(@PathVariable(name = "email") String email, Pageable pageable) {
        Page<NotificationResponseDTO> notifications = service.findByUserEmail(email, pageable);
        return ResponseEntity.ok(notifications);
    }

    @PostMapping
    @Operation(summary = "Crear nueva notificación", description = "Crea una nueva notificación con los datos proporcionados.", parameters = {@Parameter(name = "dto", description = "Detalles de la notificación")})
    public ResponseEntity<NotificationResponseDTO> create(@RequestBody @Valid NotificationRequestDTO dto) {
        NotificationResponseDTO saved = service.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
}
