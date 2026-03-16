package org.example.backendcrcoach.web.exceptions.principal_system_exceptions.user;

public class DuplicatedUserException extends RuntimeException {
    public DuplicatedUserException(String username) {super("the user already exists: " + username);}
}
