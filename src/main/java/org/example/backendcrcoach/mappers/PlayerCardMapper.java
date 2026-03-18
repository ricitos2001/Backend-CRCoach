package org.example.backendcrcoach.mappers;

import org.example.backendcrcoach.domain.dto.PlayerCardRequestDTO;
import org.example.backendcrcoach.domain.dto.PlayerCardResponseDTO;
import org.example.backendcrcoach.domain.entities.PlayerCard;

public class PlayerCardMapper {

    public static PlayerCard toEntity(PlayerCardRequestDTO dto) {
        if (dto == null) return null;
        PlayerCard card = new PlayerCard();
        card.setCardId(dto.getCardId());
        card.setName(dto.getName());
        card.setLevel(dto.getLevel());
        card.setMaxLevel(dto.getMaxLevel());
        card.setMaxEvolutionLevel(dto.getMaxEvolutionLevel());
        card.setRarity(dto.getRarity());
        card.setCount(dto.getCount());
        card.setElixirCost(dto.getElixirCost());
        card.setIconUrl(dto.getIconUrl());
        return card;
    }

    public static PlayerCardResponseDTO toDTO(PlayerCard card) {
        if (card == null) return null;
        String playerTag = card.getPlayerProfile() != null ? card.getPlayerProfile().getTag() : null;
        return new PlayerCardResponseDTO(
                card.getId(),
                card.getCardId(),
                card.getName(),
                card.getLevel(),
                card.getMaxLevel(),
                card.getMaxEvolutionLevel(),
                card.getRarity(),
                card.getCount(),
                card.getElixirCost(),
                card.getIconUrl(),
                playerTag
        );
    }
}

