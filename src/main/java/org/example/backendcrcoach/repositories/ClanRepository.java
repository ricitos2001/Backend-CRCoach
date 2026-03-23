package org.example.backendcrcoach.repositories;

import org.example.backendcrcoach.domain.entities.Clan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClanRepository extends JpaRepository<Clan, Long> {
    Optional<Clan> findByTag(String tag);
}

