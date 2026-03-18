package org.example.backendcrcoach.repositories;

import org.example.backendcrcoach.domain.entities.Battle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BattleRepository extends JpaRepository<Battle, Long> {
	boolean existsByBattleTime(String battleTime);
}

