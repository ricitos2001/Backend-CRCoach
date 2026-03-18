package org.example.backendcrcoach.repositories;

import org.example.backendcrcoach.domain.entities.Goal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GoalRepository extends JpaRepository<Goal, Long> {
    Boolean existsByTitle(String title);

    Goal getGoalByTitle(String title);

    Goal getGoalById(Long id);

    Boolean existsByTitleAndIdNot(String title, Long id);
}
