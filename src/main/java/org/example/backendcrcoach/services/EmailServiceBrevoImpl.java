package org.example.backendcrcoach.services;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Primary
public class EmailServiceBrevoImpl implements EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailServiceBrevoImpl.class);

    @Value("${brevo.api.key}")
    private String apiKey;

    @Value("${brevo.sender.email}")
    private String senderEmail;

    @Value("${brevo.sender.name}")
    private String senderName;

    private final RestTemplate restTemplate = new RestTemplate();
    private final SpringTemplateEngine templateEngine;

    private boolean enabled = false;

    public EmailServiceBrevoImpl(SpringTemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    @PostConstruct
    private void init() {
        if (apiKey == null || apiKey.isBlank()) {
            logger.warn("Brevo API key not configured (brevo.api.key). Brevo email sender DISABLED.");
            enabled = false;
            return;
        }
        if (senderEmail == null || senderEmail.isBlank()) {
            logger.warn("Brevo sender email not configured (brevo.sender.email). Brevo email sender DISABLED.");
            enabled = false;
            return;
        }
        enabled = true;
        logger.info("Brevo email sender ENABLED. Sender: {} <{}>", senderName, senderEmail);
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void sendSimpleEmail(String to, String subject, String text) {
        if (!enabled) {
            logger.info("Skipping sendSimpleEmail to {} because Brevo is disabled. Subject: {}", to, subject);
            return;
        }
        sendViaBrevo(to, subject, null, text);
    }

    @Override
    public void sendHtmlEmail(String to, String subject, String html) {
        if (!enabled) {
            logger.info("Skipping sendHtmlEmail to {} because Brevo is disabled. Subject: {}", to, subject);
            return;
        }
        sendViaBrevo(to, subject, html, null);
    }

    @Override
    public void sendTemplateEmail(String to, String subject, String templateName, Map<String, Object> model) {
        if (!enabled) {
            logger.info("Skipping sendTemplateEmail to {} because Brevo is disabled. Subject: {} Template: {}", to, subject, templateName);
            return;
        }
        if (templateName == null || templateName.isBlank()) {
            throw new IllegalArgumentException("templateName is required");
        }
        Context context = new Context();
        if (model != null) context.setVariables(model);
        String html = templateEngine.process(templateName, context);
        sendViaBrevo(to, subject, html, null);
    }

    public void sendTestEmail(String to) {
        if (!enabled) {
            logger.info("Skipping sendTestEmail to {} because Brevo is disabled.", to);
            return;
        }
        sendViaBrevo(to, "Correo de prueba", "<html><body>¡Hola! Este es un correo de prueba.</body></html>", null);
    }

    private void sendViaBrevo(String to, String subject, String htmlContent, String textContent) {
        String url = "https://api.brevo.com/v3/smtp/email";

        Map<String, Object> body = new HashMap<>();
        Map<String, String> sender = Map.of("name", senderName, "email", senderEmail);
        body.put("sender", sender);
        body.put("to", List.of(Map.of("email", to)));
        body.put("subject", subject);
        if (htmlContent != null) body.put("htmlContent", htmlContent);
        if (textContent != null) body.put("textContent", textContent);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-key", apiKey);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            logger.info("Brevo send response: status={}, body={}", response.getStatusCode(), response.getBody());
        } catch (RestClientException e) {
            logger.error("Failed to send email via Brevo to {}: {}", to, e.getMessage());
            // no relanzar para no romper la lógica principal
        }
    }
}
