package org.example.backendcrcoach.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.backendcrcoach.domain.entities.Arena;
import org.example.backendcrcoach.domain.entities.PlayerEntity;

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
    private String gameMode;
    private PlayerEntity team;
    private PlayerEntity opponent;
}

