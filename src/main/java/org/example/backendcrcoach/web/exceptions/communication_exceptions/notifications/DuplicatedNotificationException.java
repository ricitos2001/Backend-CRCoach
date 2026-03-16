package org.example.backendcrcoach.web.exceptions.communication_exceptions.notifications;

public class DuplicatedNotificationException extends RuntimeException {
  public DuplicatedNotificationException(String name) {
    super("the notification already exists: " + name);
  }

}
