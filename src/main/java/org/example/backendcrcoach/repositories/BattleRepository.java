package org.example.backendcrcoach.repositories;

import org.example.backendcrcoach.domain.entities.Battle;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
 public interface BattleRepository extends JpaRepository<Battle, Long>, JpaSpecificationExecutor<Battle> {
	Boolean existsByBattleTime(String battleTime);
	// Recuperar una batalla por su battleTime (si existe)
	Optional<Battle> findFirstByBattleTime(String battleTime);

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

	// Misma consulta pero aplicando filtros por rango temporal sobre la columna battleTime (string ISO)
	@Query("""
		SELECT b FROM Battle b
		WHERE (b.team.tag = :tag OR b.opponent.tag = :tag)
		  AND (:gameMode IS NULL OR b.gameMode.name = :gameMode)
		  AND (:from IS NULL OR b.battleTime >= :from)
		  AND (:to IS NULL OR b.battleTime <= :to)
	""")
	List<Battle> findByTeamTagWithFiltersRange(@Param("tag") String tag,
											  @Param("gameMode") String gameMode,
											  @Param("from") String from,
											  @Param("to") String to);

	// Obtener batallas recientes donde el jugador aparece en team u opponent
	List<Battle> findByTeamTagOrOpponentTagOrderByBattleTimeDesc(String teamTag, String opponentTag, Pageable pageable);
}

