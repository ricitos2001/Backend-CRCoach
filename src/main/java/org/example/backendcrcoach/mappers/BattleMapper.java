package org.example.backendcrcoach.mappers;

import org.example.backendcrcoach.domain.dto.BattleRequestDTO;
import org.example.backendcrcoach.domain.dto.BattleResponseDTO;
import org.example.backendcrcoach.domain.entities.Battle;

public class BattleMapper {

    public static Battle toEntity(BattleRequestDTO dto) {
        if (dto == null) return null;
        Battle battle = new Battle();
        battle.setType(dto.getType());
        battle.setBattleTime(dto.getBattleTime());
        battle.setIsLadderTournament(dto.getIsLadderTournament());
        battle.setDeckSelection(dto.getDeckSelection());
        battle.setIsHostedMatch(dto.getIsHostedMatch());
        battle.setLeagueNumber(dto.getLeagueNumber());
        battle.setArena(dto.getArena());
        battle.setGameMode(dto.getGameMode());
        battle.setTeam(dto.getTeam());
        battle.setOpponent(dto.getOpponent());
        return battle;
    }

    public static BattleResponseDTO toDTO(Battle battle) {
        if (battle == null) return null;
        return new BattleResponseDTO(
                battle.getId(),
                battle.getType(),
                battle.getBattleTime(),
                battle.getIsLadderTournament(),
                battle.getDeckSelection(),
                battle.getIsHostedMatch(),
                battle.getLeagueNumber(),
                battle.getArena(),
                battle.getGameMode(),
                battle.getTeam(),
                battle.getOpponent(),
                battle.getPlayerProfile() != null ? battle.getPlayerProfile().getTag() : null
        );
    }
}

