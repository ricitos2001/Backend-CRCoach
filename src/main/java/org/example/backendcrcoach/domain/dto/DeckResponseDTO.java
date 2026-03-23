package org.example.backendcrcoach.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.backendcrcoach.domain.entities.PlayerCard;

import java.util.List;

@Getter
@AllArgsConstructor
public class DeckResponseDTO {
    private Long id;
    private Long apiId;
    private String archetype;
    private List<PlayerCard> playerCards;
}

