package org.example.backendcrcoach.repositories;

import org.example.backendcrcoach.domain.entities.Session;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionRepository extends JpaRepository<Session, Long> {
    boolean existsByTitle(String title);

    Session getSessionByTitle(String title);

    Session getSessionById(Long id);

    boolean existsByTitleAndIdNot(String title, Long id);
}
