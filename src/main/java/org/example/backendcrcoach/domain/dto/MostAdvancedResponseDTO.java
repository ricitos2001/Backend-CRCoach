package org.example.backendcrcoach.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MostAdvancedResponseDTO {
    private Long id;
    private String title;
    private Double progressPercent;
}

