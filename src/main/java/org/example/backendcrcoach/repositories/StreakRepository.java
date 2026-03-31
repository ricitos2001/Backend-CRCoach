package org.example.backendcrcoach.repositories;

import org.example.backendcrcoach.domain.entities.Streak;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StreakRepository extends JpaRepository<Streak, Long> {
}

