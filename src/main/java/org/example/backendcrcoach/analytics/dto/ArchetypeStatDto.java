package org.example.backendcrcoach.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.backendcrcoach.analytics.Archetype;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ArchetypeStatDto {
	private Archetype archetype;
	private Long battles;
	private Long wins;
	private Long losses;
	private Double winRate;
	private String label;
}


