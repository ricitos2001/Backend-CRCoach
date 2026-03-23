package org.example.backendcrcoach.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class PlayerEntityResponseDTO {
    private Long id;
    private String tag;
    private String name;
    private Integer startingTrophies;
    private Integer crowns;
    private Integer kingTowerHitPoints;
    private List<Integer> princessTowersHitPoints;
    private String clanTag;
    private String clanName;
    private Integer globalRank;
    private Double elixirLeaked;
    private DeckResponseDTO playerDeck;
}

