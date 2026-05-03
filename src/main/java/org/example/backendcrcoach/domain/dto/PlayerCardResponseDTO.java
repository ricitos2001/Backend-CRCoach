package org.example.backendcrcoach.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.backendcrcoach.domain.entities.IconUrl;
import org.example.backendcrcoach.domain.enums.CardUseType;

@Getter
@AllArgsConstructor
public class PlayerCardResponseDTO {
    private Long id;
    private Integer cardId;
    private String name;
    private Integer level;
    private Integer maxLevel;
    private Integer maxEvolutionLevel;
    private String rarity;
    private Integer count;
    private Integer elixirCost;
    private IconUrl iconUrl;
    private CardUseType useType;
    private String playerTag;
}

