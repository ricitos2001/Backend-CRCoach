package org.example.backendcrcoach.repositories;

import org.example.backendcrcoach.domain.entities.Battle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BattleRepository extends JpaRepository<Battle, Long> {
	Boolean existsByBattleTime(String battleTime);

    Boolean existsByTeam(String team);

	// Obtener batallas de un jugador ordenadas por tiempo (reciente primero) con paginación
	List<Battle> findByPlayerProfileTagOrderByBattleTimeDesc(String playerTag, org.springframework.data.domain.Pageable pageable);
}

