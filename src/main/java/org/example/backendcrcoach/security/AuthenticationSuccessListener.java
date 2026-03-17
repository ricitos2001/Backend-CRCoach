package org.example.backendcrcoach.security;

import org.example.backendcrcoach.services.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class AuthenticationSuccessListener implements ApplicationListener<AuthenticationSuccessEvent> {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationSuccessListener.class);

    private final EmailService emailService;

    public AuthenticationSuccessListener(EmailService emailService) {
        this.emailService = emailService;
    }

    @Override
    public void onApplicationEvent(AuthenticationSuccessEvent event) {
        try {
            String username = event.getAuthentication().getName();
            // Enviar un simple correo de notificación de inicio de sesión
            String subject = "Nuevo inicio de sesión - CRCoach";
            Map<String, Object> model = new HashMap<>();
            model.put("username", username);
            // Usamos plantilla saludo.html si existe, fallback a texto simple
            try {
                emailService.sendTemplateEmail(username, subject, "saludo.html", model);
            } catch (Exception e) {
                logger.info("Falling back to simple email for login notification: {}", e.getMessage());
                String text = String.format("Se ha detectado un inicio de sesión en tu cuenta: %s", username);
                emailService.sendSimpleEmail(username, subject, text);
            }
            logger.info("Login notification sent to {}", username);
        } catch (Exception e) {
            logger.error("Failed to send login notification: {}", e.getMessage());
        }
    }
}
