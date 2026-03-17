package org.example.backendcrcoach.web.exceptions;

public class DuplicatedUserException extends RuntimeException {
    public DuplicatedUserException(String username) {super("the user already exists: " + username);}
}
