package org.example.backendcrcoach.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ActiveGoalsResponseDTO {
    private Long id;
    private Integer count;
    private String nearestDeadline;
    private MostAdvancedResponseDTO mostAdvanced;
}

