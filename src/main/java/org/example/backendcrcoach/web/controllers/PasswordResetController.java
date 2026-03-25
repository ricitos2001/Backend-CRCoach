package org.example.backendcrcoach.web.controllers;

import jakarta.validation.Valid;
import org.example.backendcrcoach.domain.dto.PasswordForgotRequestDTO;
import org.example.backendcrcoach.domain.dto.PasswordResetConfirmDTO;
import org.example.backendcrcoach.services.PasswordResetService;
import io.swagger.v3.oas.annotations.Operation;
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
    @Operation(summary = "Solicitar restablecimiento de contraseña", description = "Inicia el flujo de restablecimiento de contraseña enviando un correo (respuesta siempre genérica).")
    @PostMapping("/forgot")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody PasswordForgotRequestDTO request) {
        // Siempre responder igual para no filtrar existencia
        passwordResetService.requestPasswordReset(request.getEmail());
        return ResponseEntity.ok().body(java.util.Map.of("message", "Si existe una cuenta asociada, se ha enviado un correo con instrucciones."));
    }
    @Operation(summary = "Verificar token de restablecimiento", description = "Verifica si un token de restablecimiento es válido.")
    @GetMapping("/verify")
    public ResponseEntity<?> verifyToken(@RequestParam("token") String token) {
        boolean valid = passwordResetService.validateToken(token);
        if (!valid) return ResponseEntity.badRequest().body(java.util.Map.of("valid", false));
        return ResponseEntity.ok().body(java.util.Map.of("valid", true));
    }
    @Operation(summary = "Confirmar restablecimiento de contraseña", description = "Actualiza la contraseña si el token es válido y la nueva contraseña cumple requisitos.")
    @PostMapping("/reset")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody PasswordResetConfirmDTO request) {
        passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok().body(java.util.Map.of("message", "Contraseña actualizada correctamente."));
    }
}

