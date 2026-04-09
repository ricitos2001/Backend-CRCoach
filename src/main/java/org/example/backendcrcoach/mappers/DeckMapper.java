package org.example.backendcrcoach.mappers;

import org.example.backendcrcoach.domain.dto.DeckRequestDTO;
import org.example.backendcrcoach.domain.dto.DeckResponseDTO;
import org.example.backendcrcoach.domain.entities.Deck;

public class DeckMapper {

    public static Deck toEntity(DeckRequestDTO dto) {
        Deck deck = new Deck();
        deck.setArchetype(dto.getArchetype());
        deck.setPlayerCards(dto.getPlayerCards());
        return deck;
    }

    public static DeckResponseDTO toDTO(Deck deck) {
        return new DeckResponseDTO(
                deck.getId(),
                deck.getArchetype(),
                deck.getPlayerCards()
        );
    }
}

