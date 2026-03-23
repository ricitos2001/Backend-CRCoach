package org.example.backendcrcoach.mappers;

import org.example.backendcrcoach.domain.dto.ArenaRequestDTO;
import org.example.backendcrcoach.domain.dto.ArenaResponseDTO;
import org.example.backendcrcoach.domain.entities.Arena;

public class ArenaMapper {

    public static Arena toEntity(ArenaRequestDTO dto) {
        Arena arena = new Arena();
        arena.setId(dto.getId());
        arena.setName(dto.getName());
        arena.setRawName(dto.getRawName());
        return arena;
    }

    public static ArenaResponseDTO toDTO(Arena arena) {
        return new ArenaResponseDTO(
                arena.getId(),
                arena.getName(),
                arena.getRawName()
        );
    }
}

