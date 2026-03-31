package org.example.backendcrcoach.repositories;

import org.example.backendcrcoach.domain.entities.ActiveGoals;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActiveGoalsRepository extends JpaRepository<ActiveGoals, Long> {
}

