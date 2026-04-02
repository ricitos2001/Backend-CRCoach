package org.example.backendcrcoach.mappers;

import org.example.backendcrcoach.domain.dto.GameModeRequestDTO;
import org.example.backendcrcoach.domain.dto.GameModeResponseDTO;
import org.example.backendcrcoach.domain.entities.GameMode;

public class GameModeMapper {

    public static GameMode toEntity(GameModeRequestDTO dto) {
        GameMode gm = new GameMode();
        gm.setGameModeId(dto.getGameModeId());
        gm.setName(dto.getName());
        return gm;
    }

    public static GameModeResponseDTO toDTO(GameMode gm) {
        return new GameModeResponseDTO(
                gm.getId(),
                gm.getGameModeId(),
                gm.getName()
        );
    }
}

