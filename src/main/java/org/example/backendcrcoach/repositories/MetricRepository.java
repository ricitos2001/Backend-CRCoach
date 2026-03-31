package org.example.backendcrcoach.repositories;

import org.example.backendcrcoach.domain.entities.Metric;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MetricRepository extends JpaRepository<Metric, Long> {
    // Repositorio simple, consulta para comprobar si existe alguna Metric
    // referenciando una LeagueStadistic concreta.
    boolean existsByLeagueStatisticsId(Long leagueStadisticId);
}

