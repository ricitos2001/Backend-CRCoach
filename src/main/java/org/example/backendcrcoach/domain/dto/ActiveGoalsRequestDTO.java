package org.example.backendcrcoach.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ActiveGoalsRequestDTO {
    private Integer count;
    private String nearestDeadline;
    private MostAdvancedRequestDTO mostAdvanced;
}

