package org.example.backendcrcoach.mappers;

import org.example.backendcrcoach.domain.dto.ActiveGoalsRequestDTO;
import org.example.backendcrcoach.domain.dto.ActiveGoalsResponseDTO;
import org.example.backendcrcoach.domain.entities.ActiveGoals;
import org.example.backendcrcoach.domain.entities.MostAdvanced;
import org.example.backendcrcoach.domain.dto.MostAdvancedRequestDTO;

public class ActiveGoalsMapper {
    public static ActiveGoals toEntity(ActiveGoalsRequestDTO dto) {
        if (dto == null) return null;
        MostAdvanced ma = null;
        if (dto.getMostAdvanced() != null) {
            ma = org.example.backendcrcoach.mappers.MostAdvancedMapper.toEntity(dto.getMostAdvanced());
        }
        return ActiveGoals.builder().count(dto.getCount()).nearestDeadline(dto.getNearestDeadline()).mostAdvanced(ma).build();
    }

    public static ActiveGoalsResponseDTO toDTO(ActiveGoals e) {
        if (e == null) return null;
        return new ActiveGoalsResponseDTO(e.getId(), e.getCount(), e.getNearestDeadline(), MostAdvancedMapper.toDTO(e.getMostAdvanced()));
    }
}

