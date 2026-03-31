package org.example.backendcrcoach.services;

import org.example.backendcrcoach.domain.entities.PasswordResetToken;
import org.example.backendcrcoach.domain.entities.User;
import org.example.backendcrcoach.repositories.PasswordResetTokenRepository;
import org.example.backendcrcoach.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class PasswordResetService {
    private static final Logger logger = LoggerFactory.getLogger(PasswordResetService.class);

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();
    private final EmailService emailService;

    @Value("${app.password-reset.token-expiration-minutes:60}")
    private long tokenExpirationMinutes;

    @Value("${app.frontend.base-url:http://localhost:4200}")
    private String frontendBaseUrl;

    public PasswordResetService(UserRepository userRepository, PasswordResetTokenRepository tokenRepository, PasswordEncoder passwordEncoder, EmailService emailService) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    public void requestPasswordReset(String email) {
        // Responder igual si existe o no para no filtrar usuarios
        Optional<User> optionalUser = userRepository.findByEmail(email.toLowerCase());
        if (optionalUser.isEmpty()) {
            // No revelar existencia: simplemente loggear y retornar
            logger.info("Password reset requested for non-existing email: {}", email);
            return;
        }

        User user = optionalUser.get();

        // generar token seguro
        String rawToken = generateToken();
        String tokenHash = hash(rawToken);

        Instant now = Instant.now();
        PasswordResetToken token = PasswordResetToken.builder()
                .tokenHash(tokenHash)
                .user(user)
                .createdAt(now)
                .expiresAt(now.plus(tokenExpirationMinutes, ChronoUnit.MINUTES))
                .used(false)
                .build();

        tokenRepository.save(token);

        // Construir link y enviar correo
        try {
            String encoded = URLEncoder.encode(rawToken, StandardCharsets.UTF_8);
            String resetLink = String.format("%s/reset-password?token=%s", frontendBaseUrl, encoded);

            // preparar modelo para plantilla
            java.util.Map<String, Object> model = new java.util.HashMap<>();
            model.put("link", resetLink);
            model.put("user", user);

            String subject = "Restablece tu contraseña - MemoWorks";

            // enviar HTML con plantilla
            emailService.sendTemplateEmail(user.getEmail(), subject, "password-reset.html", model);

            // Log en dev
            logger.info("Password reset requested for user {}. Reset link sent to email.", user.getEmail());
        } catch (Exception e) {
            logger.error("Failed to send password reset email to {}: {}", user.getEmail(), e.getMessage());
            // fallback: log link para desarrollo
            String resetLink = String.format("%s/reset-password?token=%s", frontendBaseUrl, rawToken);
            logger.info("Password reset link (fallback log) for {}: {}", user.getEmail(), resetLink);
        }
    }

    public boolean validateToken(String rawToken) {
        String tokenHash = hash(rawToken);
        Optional<PasswordResetToken> optionalToken = tokenRepository.findByTokenHash(tokenHash);
        if (optionalToken.isEmpty()) return false;
        PasswordResetToken token = optionalToken.get();
        if (token.isUsed()) return false;
        return !token.getExpiresAt().isBefore(Instant.now());
    }

    public void resetPassword(String rawToken, String newPassword) {
        String tokenHash = hash(rawToken);
        PasswordResetToken token = tokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new IllegalArgumentException("Token inválido o expirado"));

        if (token.isUsed()) throw new IllegalArgumentException("Token ya utilizado");
        if (token.getExpiresAt().isBefore(Instant.now())) throw new IllegalArgumentException("Token expirado");

        User user = token.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        // guardar usuario
        userRepository.save(user);

        // marcar token como usado y guardar
        token.setUsed(true);
        tokenRepository.save(token);

        // invalidar otros tokens del usuario (optional): borrar tokens no usados
        // tokenRepository.deleteByUserAndUsedFalse(user); // no implementado
    }

    private String generateToken() {
        // UUID + random bytes -> base64url
        byte[] random = new byte[24];
        secureRandom.nextBytes(random);
        String uuid = UUID.randomUUID().toString();
        return uuid + Base64.getUrlEncoder().withoutPadding().encodeToString(random);
    }

    private String hash(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

}

