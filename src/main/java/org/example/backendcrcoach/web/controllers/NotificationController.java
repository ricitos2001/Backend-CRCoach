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
    @Operation(summary = "Get paginated list of notifications", description = "Retrieve a paginated list of notifications.", parameters = {@Parameter(name = "pageable", description = "Pagination information")})
    public ResponseEntity<Page<NotificationResponseDTO>> getNotifications(Pageable pageable) {
        Page<NotificationResponseDTO> notifications = service.findAll(pageable);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/myNotifications/{email}")
    @Operation(summary = "Get paginated list of notifications by user email", description = "Retrieve a paginated list of notifications associated with a specific user email.", parameters = {@Parameter(name = "email", description = "User email"), @Parameter(name = "pageable", description = "Pagination information")})
    public ResponseEntity<Page<NotificationResponseDTO>> getNotificationsByUserEmail(@PathVariable(name = "email") String email, Pageable pageable) {
        Page<NotificationResponseDTO> notifications = service.findByUserEmail(email, pageable);
        return ResponseEntity.ok(notifications);
    }

    @PostMapping
    @Operation(summary = "Create a new notification", description = "Create a new notification with the provided details.", parameters = {@Parameter(name = "dto", description = "Notification details")})
    public ResponseEntity<NotificationResponseDTO> create(@RequestBody @Valid NotificationRequestDTO dto) {
        NotificationResponseDTO saved = service.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
}
