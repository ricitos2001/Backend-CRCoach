package org.example.backendcrcoach.mappers;

import org.example.backendcrcoach.domain.dto.CardRequestDTO;
import org.example.backendcrcoach.domain.dto.CardResponseDTO;
import org.example.backendcrcoach.domain.entities.Card;
import org.example.backendcrcoach.domain.entities.IconUrl;

public class CardMapper {

    public static Card toEntity(CardRequestDTO dto) {
        if (dto == null) return null;
        Card card = new Card();
        card.setCardId(dto.getCardId());
        card.setName(dto.getName());
        card.setMaxLevel(dto.getMaxLevel());
        card.setMaxEvolutionLevel(dto.getMaxEvolutionLevel());
        card.setRarity(dto.getRarity());
        card.setElixirCost(dto.getElixirCost());
        if (dto.getIconUrl() != null) {
            IconUrl icon = new IconUrl();
            icon.setMedium(dto.getIconUrl().getMedium());
            icon.setEvolutionMedium(dto.getIconUrl().getEvolutionMedium());
            card.setIconUrl(icon);
        }
        return card;
    }

    public static CardResponseDTO toDTO(Card card) {
        if (card == null) return null;
        IconUrl iconUrl = card.getIconUrl();
        String playerTag = card.getPlayerProfile() != null ? card.getPlayerProfile().getTag() : null;
        return new CardResponseDTO(
                card.getId(),
                card.getCardId(),
                card.getName(),
                card.getMaxLevel(),
                card.getMaxEvolutionLevel(),
                card.getRarity(),
                card.getElixirCost(),
                iconUrl,
                playerTag
        );
    }
}

