package org.example.backendcrcoach.web.exceptions;

import org.example.backendcrcoach.web.exceptions.communication_exceptions.notifications.DuplicatedNotificationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validation(MethodArgumentNotValidException ex) {
        String tmp = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .findFirst()
                .map(org.springframework.validation.ObjectError::getDefaultMessage)
                .orElse(null);
        String msg = Objects.requireNonNullElse(tmp, "Validation error");
        return ResponseEntity.badRequest().body(Map.of(
                "error", "VALIDATION_ERROR",
                "message", msg,
                "timestamp", Instant.now().toString()
        ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> badRequest(IllegalArgumentException ex) {
        String msg = Objects.requireNonNullElse(ex.getMessage(), "Illegal argument");
        return ResponseEntity.badRequest().body(Map.of(
                "error", "BAD_REQUEST",
                "message", msg,
                "timestamp", Instant.now().toString()
        ));
    }

    @ExceptionHandler(DuplicatedNotificationException.class)
    public ResponseEntity<Map<String, Object>> duplicatedUser(DuplicatedNotificationException ex) {
        String msg = Objects.requireNonNullElse(ex.getMessage(), "User conflict");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "error", "CONFLICT",
                "message", msg,
                "timestamp", Instant.now().toString()
        ));
    }
}