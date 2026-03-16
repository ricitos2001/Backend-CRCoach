package org.example.backendcrcoach.web.exceptions.principal_system_exceptions.goal;

public class GoalNotFoundException extends RuntimeException {
    public GoalNotFoundException(String title) {super("Goal not found: " + title);}
    public GoalNotFoundException(Long id) {super("Goal not found: " + id);}
}
