package org.example.backendcrcoach.security;

import org.example.backendcrcoach.security.jwt.JwtRequestFilter;
import org.example.backendcrcoach.security.user.CustomUserDetailsService;
import org.example.backendcrcoach.services.communication_services.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.HashMap;
import java.util.List;
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
