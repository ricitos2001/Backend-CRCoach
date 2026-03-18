package org.example.backendcrcoach.repositories;

import org.example.backendcrcoach.domain.entities.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findAllByOrderByCreatedAtDesc();

    Boolean existsByTitle(String title);

    Page<Notification> findByUserEmail(String userEmail, Pageable pageable);
}
