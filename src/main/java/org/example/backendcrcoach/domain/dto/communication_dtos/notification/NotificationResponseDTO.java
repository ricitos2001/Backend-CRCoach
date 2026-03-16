package org.example.backendcrcoach.domain.dto.communication_dtos.notification;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Date;

@Getter
@AllArgsConstructor
public class NotificationResponseDTO {
    private Long id;
    private String title;
    private String message;
    private Date createdAt;
    private String userEmail;
}

