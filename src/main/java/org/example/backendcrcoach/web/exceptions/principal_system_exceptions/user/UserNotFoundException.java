package org.example.backendcrcoach.web.exceptions.principal_system_exceptions.user;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String username) {super("user not found: " + username);}
    public UserNotFoundException(Long id) {super("user not found: " + id);}
}
