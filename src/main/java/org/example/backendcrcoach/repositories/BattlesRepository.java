package org.example.backendcrcoach.repositories;

import org.example.backendcrcoach.domain.entities.Battles;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BattlesRepository extends JpaRepository<Battles, Long> {
}

