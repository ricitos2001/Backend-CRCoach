package org.example.backendcrcoach.web.exceptions;

public class DuplicatedGoalException extends RuntimeException {
    public DuplicatedGoalException(String title) {super("the goal already exists: " + title);}
}
