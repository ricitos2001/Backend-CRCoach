package org.example.backendcrcoach.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StreakResponseDTO {
    private Long id;
    private Integer current;
    private String type;
}

