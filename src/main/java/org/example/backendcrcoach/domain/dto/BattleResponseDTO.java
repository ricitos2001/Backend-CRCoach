package org.example.backendcrcoach.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.backendcrcoach.domain.entities.Arena;
import org.example.backendcrcoach.domain.entities.PlayerEntity;
import org.example.backendcrcoach.domain.entities.GameMode;

@Getter
@AllArgsConstructor
public class BattleResponseDTO {
    private Long id;
    private String type;
    private String battleTime;
    private Boolean isLadderTournament;
    private String deckSelection;
    private Boolean isHostedMatch;
    private Integer leagueNumber;

    private Arena arena;
    private GameMode gameMode;
    private PlayerEntity team;
    private PlayerEntity opponent;
}

