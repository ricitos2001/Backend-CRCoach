package org.example.backendcrcoach.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.backendcrcoach.domain.entities.Arena;
import org.example.backendcrcoach.domain.entities.PlayerEntity;
import org.example.backendcrcoach.domain.entities.GameMode;

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
    // Campos complejos
    private Arena arena;
    private GameMode gameMode;
    private PlayerEntity team;
    private PlayerEntity opponent;
}

