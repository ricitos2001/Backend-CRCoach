package org.example.backendcrcoach.repositories;

import org.example.backendcrcoach.domain.entities.PlayerProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlayerProfileRepository extends JpaRepository<PlayerProfile, Long> {
    Optional<PlayerProfile> findByTag(String tag);
    Boolean existsByTag(String tag);
}

