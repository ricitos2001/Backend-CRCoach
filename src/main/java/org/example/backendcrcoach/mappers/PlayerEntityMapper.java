package org.example.backendcrcoach.mappers;

import org.example.backendcrcoach.domain.dto.PlayerEntityRequestDTO;
import org.example.backendcrcoach.domain.dto.PlayerEntityResponseDTO;
import org.example.backendcrcoach.domain.entities.PlayerEntity;

import java.util.ArrayList;
import java.util.List;

public class PlayerEntityMapper {

    public static PlayerEntity toEntity(PlayerEntityRequestDTO dto) {
        PlayerEntity entity = new PlayerEntity();
        entity.setTag(dto.getTag());
        entity.setName(dto.getName());
        entity.setStartingTrophies(dto.getStartingTrophies());
        entity.setTrophyChange(dto.getTrophyChange());
        entity.setCrowns(dto.getCrowns());
        entity.setKingTowerHitPoints(dto.getKingTowerHitPoints());
        entity.setPrincessTowersHitPoints(dto.getPrincessTowersHitPoints());
        entity.setGlobalRank(dto.getGlobalRank());
        entity.setElixirLeaked(dto.getElixirLeaked());

        if (dto.getPlayerDeck() != null) {
            entity.setPlayerDeck(DeckMapper.toEntity(dto.getPlayerDeck()));
        }

        return entity;
    }

    public static PlayerEntityResponseDTO toDTO(PlayerEntity entity) {

        return new PlayerEntityResponseDTO(
                entity.getId(),
                entity.getTag(),
                entity.getName(),
                entity.getStartingTrophies(),
                entity.getTrophyChange(),
                entity.getCrowns(),
                entity.getKingTowerHitPoints(),
                entity.getPrincessTowersHitPoints() != null ? new ArrayList<>(entity.getPrincessTowersHitPoints()) : null,
                entity.getClan() != null ? entity.getClan().getTag() : null,
                entity.getClan() != null ? entity.getClan().getName() : null,
                entity.getGlobalRank(),
                entity.getElixirLeaked(),
                entity.getPlayerDeck() != null ? DeckMapper.toDTO(entity.getPlayerDeck()) : null
        );
    }
}

