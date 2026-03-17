package org.example.backendcrcoach.web.exceptions;

public class DuplicatedNotificationException extends RuntimeException {
  public DuplicatedNotificationException(String name) {
    super("the notification already exists: " + name);
  }

}
