package org.example.backendcrcoach.repositories;

import org.example.backendcrcoach.domain.entities.PlayerDeck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlayerDeckRepository extends JpaRepository<PlayerDeck, Long> {
    List<PlayerDeck> findByPlayerProfileTag(String tag);
}

