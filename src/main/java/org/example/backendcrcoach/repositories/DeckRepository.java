package org.example.backendcrcoach.repositories;

import org.example.backendcrcoach.domain.entities.Deck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeckRepository extends JpaRepository<Deck, Long> {
    // ahora no soportamos búsqueda por apiId (campo eliminado)
}

