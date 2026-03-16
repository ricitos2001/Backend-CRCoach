package org.example.backendcrcoach.web.controllers.other_controllers;

import jakarta.validation.Valid;
import org.example.backendcrcoach.domain.dto.other_dtos.PasswordForgotRequestDTO;
import org.example.backendcrcoach.domain.dto.other_dtos.PasswordResetConfirmDTO;
import org.example.backendcrcoach.services.other_services.PasswordResetService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth/password")
@Validated
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/forgot")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody PasswordForgotRequestDTO request) {
        // Siempre responder igual para no filtrar existencia
        passwordResetService.requestPasswordReset(request.getEmail());
        return ResponseEntity.ok().body(java.util.Map.of("message", "Si existe una cuenta asociada, se ha enviado un correo con instrucciones."));
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verifyToken(@RequestParam("token") String token) {
        boolean valid = passwordResetService.validateToken(token);
        if (!valid) return ResponseEntity.badRequest().body(java.util.Map.of("valid", false));
        return ResponseEntity.ok().body(java.util.Map.of("valid", true));
    }

    @PostMapping("/reset")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody PasswordResetConfirmDTO request) {
        passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok().body(java.util.Map.of("message", "Contraseña actualizada correctamente."));
    }
}

