package org.example.backendcrcoach.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BattleRequestDTO {
    private String type;
    private String battleTime;
    private Boolean isLadderTournament;
    private String deckSelection;
    private Boolean isHostedMatch;
    private Integer leagueNumber;
    private String playerTag;

    // Campos complejos como JSON crudo
    private String arena;
    private String gameMode;
    private String team;
    private String opponent;
}

