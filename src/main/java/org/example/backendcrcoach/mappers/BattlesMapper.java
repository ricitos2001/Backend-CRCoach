package org.example.backendcrcoach.mappers;

import org.example.backendcrcoach.domain.dto.BattlesRequestDTO;
import org.example.backendcrcoach.domain.dto.BattlesResponseDTO;
import org.example.backendcrcoach.domain.entities.Battles;

public class BattlesMapper {
    public static Battles toEntity(BattlesRequestDTO dto) {
        if (dto == null) return null;
        return Battles.builder().total(dto.getTotal()).last24h(dto.getLast24h()).build();
    }

    public static BattlesResponseDTO toDTO(Battles e) {
        if (e == null) return null;
        return new BattlesResponseDTO(e.getId(), e.getTotal(), e.getLast24h());
    }
}

