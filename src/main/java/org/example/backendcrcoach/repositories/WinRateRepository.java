package org.example.backendcrcoach.repositories;

import org.example.backendcrcoach.domain.entities.WinRate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WinRateRepository extends JpaRepository<WinRate, Long> {
}

