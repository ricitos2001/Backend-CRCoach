package org.example.backendcrcoach.web.exceptions.principal_system_exceptions.session;

public class SessionNotFoundException extends RuntimeException {
    public SessionNotFoundException(String title) {super("Session not found: " + title);}
    public SessionNotFoundException(Long id) {super("Session not found: " + id);}
}
