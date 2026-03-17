package org.example.backendcrcoach.web.controllers;

import org.example.backendcrcoach.services.EmailService;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@Profile("dev")
public class DevEmailController {
    private final EmailService emailService;

    public DevEmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @GetMapping("/dev/send-test-email")
    public ResponseEntity<String> sendTestEmail(@RequestParam String to) {
        String subject = "Prueba de correo - MemoWorks";
        Map<String, Object> model = new HashMap<>();
        model.put("link", "http://example.com/test-link");
        model.put("user", null);
        emailService.sendTemplateEmail(to, subject, "password-reset.html", model);
        return ResponseEntity.ok("Correo enviado (si la configuración SMTP está correcta)");
    }
}

