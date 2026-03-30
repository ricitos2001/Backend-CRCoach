package org.example.backendcrcoach.repositories;

import org.example.backendcrcoach.domain.entities.LeagueStadistic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LeagueStadisticRepository extends JpaRepository<LeagueStadistic, Long> {
}

