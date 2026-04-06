package org.example.backendcrcoach.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.backendcrcoach.domain.entities.IconUrl;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CardRequestDTO {
    private Integer cardId;
    private String name;
    private Integer maxLevel;
    private Integer maxEvolutionLevel;
    private String rarity;
    private Integer elixirCost;
    private IconUrl iconUrl;
}

