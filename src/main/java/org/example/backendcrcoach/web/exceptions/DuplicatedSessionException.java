package org.example.backendcrcoach.web.exceptions;

public class DuplicatedSessionException extends RuntimeException {
    public DuplicatedSessionException(String title) {super("the session already exists: " + title);}

}
