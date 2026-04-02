package org.example.backendcrcoach.repositories;

import org.example.backendcrcoach.domain.entities.GameMode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GameModeRepository extends JpaRepository<GameMode, Long> {
    Optional<GameMode> findByGameModeId(Integer gameModeId);
    Optional<GameMode> findByName(String name);
}

