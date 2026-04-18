package org.example.backendcrcoach.repositories;

import org.example.backendcrcoach.domain.entities.Metric;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MetricRepository extends JpaRepository<Metric, Long> {
    // Repositorio simple, consulta para comprobar si existe alguna Metric
    // referenciando una LeagueStadistic concreta.
    boolean existsByLeagueStatisticsId(Long leagueStadisticId);

    // Buscar la Metric asociada a una LeagueStadistic por su id (si existe)
    Optional<Metric> findByLeagueStatisticsId(Long leagueStadisticId);
}

