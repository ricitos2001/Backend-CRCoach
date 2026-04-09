package org.example.backendcrcoach.mappers;

import org.example.backendcrcoach.domain.dto.DeckRequestDTO;
import org.example.backendcrcoach.domain.dto.DeckResponseDTO;
import org.example.backendcrcoach.domain.entities.Deck;
import org.example.backendcrcoach.domain.entities.PlayerCard;

import java.util.List;

public class DeckMapper {

    public static Deck toEntity(DeckRequestDTO dto) {
        Deck deck = new Deck();
        deck.setArchetype(dto.getArchetype());
        // Copiar la lista para no mantener referencias a colecciones gestionadas por Hibernate
        if (dto.getPlayerCards() != null) {
            deck.setPlayerCards(List.copyOf(dto.getPlayerCards()));
        } else {
            deck.setPlayerCards(null);
        }
        return deck;
    }

    public static DeckResponseDTO toDTO(Deck deck) {
        // Copiar la lista a una colección inmodificable para desligarla de la implementación de Hibernate
        List<PlayerCard> playerCards = null;
        if (deck.getPlayerCards() != null) {
            playerCards = List.copyOf(deck.getPlayerCards());
        }
        return new DeckResponseDTO(
                deck.getId(),
                deck.getArchetype(),
                playerCards
        );
    }
}

