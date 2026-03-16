package org.example.backendcrcoach.mappers.communication_mappers;

import org.example.backendcrcoach.domain.dto.communication_dtos.notification.NotificationRequestDTO;
import org.example.backendcrcoach.domain.dto.communication_dtos.notification.NotificationResponseDTO;
import org.example.backendcrcoach.domain.entities.communication_entities.Notification;

public class NotificationMapper {
    public static Notification toEntity(NotificationRequestDTO dto) {
        Notification notification = new Notification();
        notification.setTitle(dto.getTitle());
        notification.setMessage(dto.getMessage());
        notification.setCreatedAt(dto.getCreatedAt());
        notification.setUserEmail(dto.getUserEmail());
        return notification;
    }

    public static NotificationResponseDTO toDTO(Notification notification) {
        return new NotificationResponseDTO(
                notification.getId(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getCreatedAt(),
                notification.getUserEmail()
        );
    }
}