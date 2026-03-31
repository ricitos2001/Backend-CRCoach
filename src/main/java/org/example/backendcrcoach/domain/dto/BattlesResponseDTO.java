package org.example.backendcrcoach.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BattlesResponseDTO {
    private Long id;
    private Integer total;
    private Integer last24h;
}

