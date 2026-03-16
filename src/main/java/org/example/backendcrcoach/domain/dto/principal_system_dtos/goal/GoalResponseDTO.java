package org.example.backendcrcoach.domain.dto.principal_system_dtos.goal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.backendcrcoach.domain.entities.principal_system_entities.User;
import org.example.backendcrcoach.domain.enums.GoalStatus;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class GoalResponseDTO {
    private Long id;
    private String title;
    private String description;
    private String metricType;
    private Double targetValue;
    private Double currentValue;
    private GoalStatus status;
    private String deadline;
    private LocalDateTime createdAt;
    private User user;
}
