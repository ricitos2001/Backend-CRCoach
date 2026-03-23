package org.example.backendcrcoach.repositories;

import org.example.backendcrcoach.domain.entities.PlayerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlayerEntityRepository extends JpaRepository<PlayerEntity, Long> {
    Optional<PlayerEntity> findByTag(String tag);
    Boolean existsByTag(String tag);
}

