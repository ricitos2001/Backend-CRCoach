package org.example.backendcrcoach.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PlayerSummaryDto {
    private String playerTag;
    private Integer trophies;
    private Double winRateLast25;
    private Double winRateLast7d;
    private Integer currentStreak;
    private Long totalBattles;
    private String weakestArchetype;
    private String strongestArchetype;
    private Double avgElixirLastDeck;
}

