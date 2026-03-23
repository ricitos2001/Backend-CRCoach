package org.example.backendcrcoach.repositories;

import org.example.backendcrcoach.domain.entities.Arena;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ArenaRepository extends JpaRepository<Arena, Long> {
	Optional<Arena> findByRawName(String rawName);
	Optional<Arena> findByName(String name);
}

