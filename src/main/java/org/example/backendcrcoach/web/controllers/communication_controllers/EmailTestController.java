package org.example.backendcrcoach.web.controllers.communication_controllers;

import org.example.backendcrcoach.services.communication_services.EmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/internal/email")
@Validated
public class EmailTestController {

    private final EmailService emailService;

    public EmailTestController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/test")
    public ResponseEntity<?> sendTest(@RequestBody Map<String, Object> body) {
        String email = (String) body.get("email");
        boolean template = Boolean.TRUE.equals(body.get("template"));
        if (email == null || email.isBlank()) return ResponseEntity.badRequest().body(Map.of("error", "email is required"));

        if (template) {
            // crear enlace de reset simulado
            String rawToken = UUID.randomUUID().toString();
            String encoded = URLEncoder.encode(rawToken, StandardCharsets.UTF_8);
            String resetLink = String.format("%s/reset-password?token=%s", "http://localhost:4200", encoded);
            Map<String, Object> model = Map.of("link", resetLink, "user", Map.of("email", email));
            emailService.sendTemplateEmail(email, "Restablece tu contraseña - MemoWorks", "password-reset.html", model);
            return ResponseEntity.ok(Map.of("sent", true, "type", "template"));
        } else {
            // usar sendHtmlEmail que está en la interfaz
            String html = "<html><body>Correo de prueba desde MemoWorks para " + email + "</body></html>";
            emailService.sendHtmlEmail(email, "Correo de prueba", html);
            return ResponseEntity.ok(Map.of("sent", true, "type", "simple"));
        }
    }
}
