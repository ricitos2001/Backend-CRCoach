package org.example.backendcrcoach.mappers;

import org.example.backendcrcoach.domain.dto.MostAdvancedRequestDTO;
import org.example.backendcrcoach.domain.dto.MostAdvancedResponseDTO;
import org.example.backendcrcoach.domain.entities.MostAdvanced;

public class MostAdvancedMapper {
    public static MostAdvanced toEntity(MostAdvancedRequestDTO dto) {
        if (dto == null) return null;
        return MostAdvanced.builder().title(dto.getTitle()).progressPercent(dto.getProgressPercent()).build();
    }

    public static MostAdvancedResponseDTO toDTO(MostAdvanced e) {
        if (e == null) return null;
        return new MostAdvancedResponseDTO(e.getId(), e.getTitle(), e.getProgressPercent());
    }
}

