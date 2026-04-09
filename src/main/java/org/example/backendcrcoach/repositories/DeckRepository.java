package org.example.backendcrcoach.repositories;

import org.example.backendcrcoach.domain.entities.Deck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeckRepository extends JpaRepository<Deck, Long> {
    @Query("select d from Deck d left join fetch d.playerCards where d.id = :id")
    Optional<Deck> findByIdWithPlayerCards(@Param("id") Long id);

    @Query("select distinct d from Deck d left join fetch d.playerCards")
    List<Deck> findAllWithPlayerCards();
}

