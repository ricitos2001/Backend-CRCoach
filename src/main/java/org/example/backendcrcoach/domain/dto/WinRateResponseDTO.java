package org.example.backendcrcoach.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WinRateResponseDTO {
    private Long id;
    private Double last25Battles;
    private Double last7Days;
}

