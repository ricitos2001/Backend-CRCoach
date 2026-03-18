package org.example.backendcrcoach.mappers;

import org.example.backendcrcoach.domain.dto.PlayerDeckRequestDTO;
import org.example.backendcrcoach.domain.dto.PlayerDeckResponseDTO;
import org.example.backendcrcoach.domain.entities.PlayerCard;
import org.example.backendcrcoach.domain.entities.PlayerDeck;
import org.example.backendcrcoach.domain.entities.PlayerProfile;

import java.util.List;
import java.util.stream.Collectors;

public class PlayerDeckMapper {

    public static PlayerDeckResponseDTO toDTO(PlayerDeck deck) {
        if (deck == null) return null;
        List playerCards = deck.getPlayerCards() == null ? java.util.List.of() : deck.getPlayerCards().stream().map(PlayerCardMapper::toDTO).collect(Collectors.toList());
        String tag = deck.getPlayerProfile() != null ? deck.getPlayerProfile().getTag() : null;
        return new PlayerDeckResponseDTO(deck.getId(), deck.getName(), playerCards, tag);
    }

    public static PlayerDeck toEntity(PlayerDeckRequestDTO dto, List<PlayerCard> cards, PlayerProfile profile) {
        if (dto == null) return null;
        PlayerDeck deck = new PlayerDeck();
        deck.setName(dto.getName());
        deck.setPlayerCards(cards == null ? java.util.List.of() : cards);
        deck.setPlayerProfile(profile);
        return deck;
    }
}

