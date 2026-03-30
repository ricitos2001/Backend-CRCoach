package org.example.backendcrcoach.mappers;

import org.example.backendcrcoach.domain.dto.LeagueStadisticRequestDTO;
import org.example.backendcrcoach.domain.dto.LeagueStadisticResponseDTO;
import org.example.backendcrcoach.domain.entities.LeagueStadistic;
// ...existing code...

public class LeagueStadisticMapper {

    public static LeagueStadistic toEntity(LeagueStadisticRequestDTO dto) {
        if (dto == null) return null;
        LeagueStadistic ls = new LeagueStadistic();
        ls.setCurrentSeason(dto.getCurrentSeason());
        ls.setPreviousSeason(dto.getPreviousSeason());
        ls.setBestSeason(dto.getBestSeason());
        return ls;
    }

    public static LeagueStadisticResponseDTO toDTO(LeagueStadistic entity) {
        if (entity == null) return null;
        return new LeagueStadisticResponseDTO(
                entity.getCurrentSeason(),
                entity.getPreviousSeason(),
                entity.getBestSeason()
        );
    }
}


