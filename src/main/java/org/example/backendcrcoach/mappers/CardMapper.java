package org.example.backendcrcoach.mappers;

import org.example.backendcrcoach.domain.dto.CardRequestDTO;
import org.example.backendcrcoach.domain.dto.CardResponseDTO;
import org.example.backendcrcoach.domain.entities.Card;
import org.example.backendcrcoach.domain.entities.IconUrl;

public class CardMapper {

    public static Card toEntity(CardRequestDTO dto) {
        Card card = new Card();
        card.setCardId(dto.getCardId());
        card.setName(dto.getName());
        card.setMaxLevel(dto.getMaxLevel());
        card.setMaxEvolutionLevel(dto.getMaxEvolutionLevel());
        card.setRarity(dto.getRarity());
        card.setElixirCost(dto.getElixirCost());
        card.setIconUrl(dto.getIconUrl());
        return card;
    }

    public static CardResponseDTO toDTO(Card card) {
        return new CardResponseDTO(
                card.getId(),
                card.getCardId(),
                card.getName(),
                card.getMaxLevel(),
                card.getMaxEvolutionLevel(),
                card.getRarity(),
                card.getElixirCost(),
                card.getIconUrl()
        );
    }
}

