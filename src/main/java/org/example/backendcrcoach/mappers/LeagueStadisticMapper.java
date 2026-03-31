package org.example.backendcrcoach.mappers;

import org.example.backendcrcoach.domain.dto.LeagueStadisticRequestDTO;
import org.example.backendcrcoach.domain.dto.LeagueStadisticResponseDTO;
import org.example.backendcrcoach.domain.entities.LeagueStadistic;
// ...existing code...

public class LeagueStadisticMapper {

    public static LeagueStadistic toEntity(LeagueStadisticRequestDTO dto) {
        LeagueStadistic ls = new LeagueStadistic();
        ls.setCurrentSeason(dto.getCurrentSeason());
        ls.setPreviousSeason(dto.getPreviousSeason());
        ls.setBestSeason(dto.getBestSeason());
        return ls;
    }

    public static LeagueStadisticResponseDTO toDTO(LeagueStadistic entity) {
        return new LeagueStadisticResponseDTO(
                entity.getCurrentSeason(),
                entity.getPreviousSeason(),
                entity.getBestSeason()
        );
    }
}


