package org.example.backendcrcoach.repositories;

import org.example.backendcrcoach.domain.entities.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {
    Boolean existsByCardId(Integer cardId);
    java.util.Optional<Card> findByCardId(Integer cardId);
}

