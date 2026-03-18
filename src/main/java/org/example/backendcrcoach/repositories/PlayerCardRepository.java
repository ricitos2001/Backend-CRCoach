package org.example.backendcrcoach.repositories;

import org.example.backendcrcoach.domain.entities.PlayerCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlayerCardRepository extends JpaRepository<PlayerCard, Long> {
    Boolean existsByCardId(Integer cardId);
    List<PlayerCard> findByPlayerProfileTag(String tag);
    Boolean existsByCardIdAndPlayerProfileTag(Integer cardId, String tag);
}

