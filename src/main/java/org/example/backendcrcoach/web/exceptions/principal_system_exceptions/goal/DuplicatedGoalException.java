package org.example.backendcrcoach.web.exceptions.principal_system_exceptions.goal;

public class DuplicatedGoalException extends RuntimeException {
    public DuplicatedGoalException(String title) {super("the goal already exists: " + title);}
}
