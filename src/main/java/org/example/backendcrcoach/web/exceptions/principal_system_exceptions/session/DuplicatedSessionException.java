package org.example.backendcrcoach.web.exceptions.principal_system_exceptions.session;

public class DuplicatedSessionException extends RuntimeException {
    public DuplicatedSessionException(String title) {super("the session already exists: " + title);}

}
