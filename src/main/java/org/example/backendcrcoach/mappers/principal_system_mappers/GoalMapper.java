package org.example.backendcrcoach.mappers.principal_system_mappers;

import org.example.backendcrcoach.domain.dto.principal_system_dtos.goal.GoalRequestDTO;
import org.example.backendcrcoach.domain.dto.principal_system_dtos.goal.GoalResponseDTO;
import org.example.backendcrcoach.domain.entities.principal_system_entities.Goal;

public class GoalMapper {
    public static Goal toEntity(GoalRequestDTO dto) {
        Goal goal = new Goal();
        goal.setTitle(dto.getTitle());
        goal.setDescription(dto.getDescription());
        goal.setMetricType(dto.getMetricType());
        goal.setTargetValue(dto.getTargetValue());
        goal.setCurrentValue(dto.getCurrentValue());
        goal.setStatus(dto.getStatus());
        goal.setDeadline(dto.getDeadline());
        goal.setCreatedAt(dto.getCreatedAt());
        goal.setUser(dto.getUser());
        return goal;
    }

    public static GoalResponseDTO toDTO(Goal goal) {
        return new GoalResponseDTO(
                goal.getId(),
                goal.getTitle(),
                goal.getDescription(),
                goal.getMetricType(),
                goal.getTargetValue(),
                goal.getCurrentValue(),
                goal.getStatus(),
                goal.getDeadline(),
                goal.getCreatedAt(),
                goal.getUser()
        );
    }
}
