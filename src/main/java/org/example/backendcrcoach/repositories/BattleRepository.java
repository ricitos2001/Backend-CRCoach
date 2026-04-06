package org.example.backendcrcoach.repositories;

import org.example.backendcrcoach.domain.entities.Battle;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
 public interface BattleRepository extends JpaRepository<Battle, Long>, JpaSpecificationExecutor<Battle> {
	Boolean existsByBattleTime(String battleTime);

	// Obtener batallas de un jugador ordenadas por tiempo (reciente primero) con paginación
	List<Battle> findByTeamTagOrderByBattleTimeDesc(String playerTag, Pageable pageable);

	// Buscar batallas filtrando por el tag del jugador (puede estar en team u opponent) y modo de juego
	@Query("""
		SELECT b FROM Battle b
		WHERE (b.team.tag = :tag OR b.opponent.tag = :tag)
		  AND (:gameMode IS NULL OR b.gameMode.name = :gameMode)
	""")
	List<Battle> findByTeamTagWithFilters(@Param("tag") String tag,
										  @Param("gameMode") String gameMode);

	// Misma consulta pero aplicando filtros por rango temporal sobre la columna battleTimeTs (tipo Instant)
	@Query("""
		SELECT b FROM Battle b
		WHERE (b.team.tag = :tag OR b.opponent.tag = :tag)
		  AND (:gameMode IS NULL OR b.gameMode.name = :gameMode)
		  AND (:from IS NULL OR b.battleTimeTs >= :from)
		  AND (:to IS NULL OR b.battleTimeTs <= :to)
	""")
	List<Battle> findByTeamTagWithFiltersRange(@Param("tag") String tag,
											  @Param("gameMode") String gameMode,
											  @Param("from") Instant from,
											  @Param("to") Instant to);

	// Obtener batallas recientes donde el jugador aparece en team u opponent
	List<Battle> findByTeamTagOrOpponentTagOrderByBattleTimeDesc(String teamTag, String opponentTag, Pageable pageable);
}

