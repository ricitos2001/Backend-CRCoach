package org.example.backendcrcoach.mappers;

import org.example.backendcrcoach.domain.dto.WinRateRequestDTO;
import org.example.backendcrcoach.domain.dto.WinRateResponseDTO;
import org.example.backendcrcoach.domain.entities.WinRate;

public class WinRateMapper {
    public static WinRate toEntity(WinRateRequestDTO dto) {
        if (dto == null) return null;
        return WinRate.builder()
                .last25Battles(dto.getLast25Battles())
                .last7Days(dto.getLast7Days())
                .build();
    }

    public static WinRateResponseDTO toDTO(WinRate entity) {
        if (entity == null) return null;
        return new WinRateResponseDTO(entity.getId(), entity.getLast25Battles(), entity.getLast7Days());
    }
}

