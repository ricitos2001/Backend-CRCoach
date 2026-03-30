package org.example.backendcrcoach.repositories;

import org.example.backendcrcoach.domain.entities.Season;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SeasonRepository extends JpaRepository<Season, Long> {
    Optional<Season> findBySeasonId(String seasonId);
}

