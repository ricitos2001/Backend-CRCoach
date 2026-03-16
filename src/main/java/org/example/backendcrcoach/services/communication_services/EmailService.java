package org.example.backendcrcoach.services.communication_services;

import java.util.Map;

public interface EmailService {
    void sendSimpleEmail(String to, String subject, String text);
    void sendHtmlEmail(String to, String subject, String html);
    void sendTemplateEmail(String to, String subject, String templateName, Map<String, Object> model);
}

