package org.example.backendcrcoach.services;

import org.example.backendcrcoach.domain.dto.NotificationRequestDTO;
import org.example.backendcrcoach.domain.dto.NotificationResponseDTO;
import org.example.backendcrcoach.domain.entities.Notification;
import org.example.backendcrcoach.mappers.NotificationMapper;
import org.example.backendcrcoach.repositories.NotificationRepository;
import org.example.backendcrcoach.web.exceptions.DuplicatedNotificationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


@Service
public class NotificationService {

    private final NotificationRepository repository;

    public NotificationService(NotificationRepository repository) {
        this.repository = repository;
    }

    public Page<NotificationResponseDTO> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(NotificationMapper::toDTO);
    }

    public Page<NotificationResponseDTO> findByUserEmail(String email, Pageable pageable) {
        return repository.findByUserEmail(email, pageable).map(NotificationMapper::toDTO);
    }

    public NotificationResponseDTO create(NotificationRequestDTO dto) {
        if (repository.existsByTitle(dto.getTitle())) {
            throw new DuplicatedNotificationException(dto.getTitle());
        } else {
            Notification notification = NotificationMapper.toEntity(dto);
            if (notification.getTitle() != null) notification.setTitle(notification.getTitle().toLowerCase());
            if (notification.getMessage() != null) notification.setMessage(notification.getMessage().toLowerCase());
            if (notification.getCreatedAt() != null) notification.setCreatedAt(notification.getCreatedAt());
            if (notification.getUserEmail() != null) notification.setUserEmail(notification.getUserEmail());
            Notification savedNotification = repository.save(notification);
            return NotificationMapper.toDTO(savedNotification);
        }
    }
}
