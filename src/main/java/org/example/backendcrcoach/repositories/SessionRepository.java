package org.example.backendcrcoach.repositories;

import org.example.backendcrcoach.domain.entities.Session;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionRepository extends JpaRepository<Session, Long> {
    Boolean existsByTitle(String title);

    Session getSessionByTitle(String title);

    Session getSessionById(Long id);

    Boolean existsByTitleAndIdNot(String title, Long id);
}
