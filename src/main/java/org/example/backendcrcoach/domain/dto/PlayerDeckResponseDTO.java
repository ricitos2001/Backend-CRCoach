package org.example.backendcrcoach.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class PlayerDeckResponseDTO {
    private Long id;
    private String name;
    private List<PlayerCardResponseDTO> playerCards;
    private String playerTag;
}

