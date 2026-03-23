package org.example.backendcrcoach.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.backendcrcoach.domain.entities.Arena;
import org.example.backendcrcoach.domain.entities.PlayerEntity;

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
    // Campos complejos como JSON crudo
    private Arena arena;
    private String gameMode;
    private PlayerEntity team;
    private PlayerEntity opponent;
    private String playerTag;

}

