package org.example.backendcrcoach.repositories.communication_repositories;

import org.example.backendcrcoach.domain.entities.communication_entities.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findAllByOrderByCreatedAtDesc();

    boolean existsByTitle(String title);

    Page<Notification> findByUserEmail(String userEmail, Pageable pageable);
}
