package org.example.backendcrcoach.repositories.principal_system_repositories;

import org.example.backendcrcoach.domain.entities.principal_system_entities.Goal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GoalRepository extends JpaRepository<Goal, Long> {
    boolean existsByTitle(String title);

    Goal getGoalByTitle(String title);

    Goal getGoalById(Long id);

    boolean existsByTitleAndIdNot(String title, Long id);
}
