package org.example.backendcrcoach.mappers;

import org.example.backendcrcoach.domain.dto.StreakRequestDTO;
import org.example.backendcrcoach.domain.dto.StreakResponseDTO;
import org.example.backendcrcoach.domain.entities.Streak;

public class StreakMapper {
    public static Streak toEntity(StreakRequestDTO dto) {
        if (dto == null) return null;
        return Streak.builder().current(dto.getCurrent()).type(dto.getType()).build();
    }

    public static StreakResponseDTO toDTO(Streak e) {
        if (e == null) return null;
        return new StreakResponseDTO(e.getId(), e.getCurrent(), e.getType());
    }
}

