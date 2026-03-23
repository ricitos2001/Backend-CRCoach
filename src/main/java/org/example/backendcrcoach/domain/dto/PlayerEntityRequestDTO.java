package org.example.backendcrcoach.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlayerEntityRequestDTO {
    private String tag;
    private String name;
    private Integer startingTrophies;
    private Integer crowns;
    private Integer kingTowerHitPoints;
    private List<Integer> princessTowersHitPoints;
    private Long clanId;
    private Integer globalRank;
    private Double elixirLeaked;
    private DeckRequestDTO playerDeck;
}

