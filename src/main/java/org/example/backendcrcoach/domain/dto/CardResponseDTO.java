package org.example.backendcrcoach.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.backendcrcoach.domain.entities.IconUrl;

@Getter
@AllArgsConstructor
public class CardResponseDTO {
	private Long id;
	private Integer cardId;
	private String name;
	private Integer maxLevel;
	private Integer maxEvolutionLevel;
	private String rarity;
	private Integer elixirCost;
	private IconUrl iconUrl;
}


