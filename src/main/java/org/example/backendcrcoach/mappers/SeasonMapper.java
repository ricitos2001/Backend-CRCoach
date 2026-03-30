package org.example.backendcrcoach.mappers;

import org.example.backendcrcoach.domain.dto.SeasonRequestDTO;
import org.example.backendcrcoach.domain.dto.SeasonResponseDTO;
import org.example.backendcrcoach.domain.entities.Season;

public class SeasonMapper {

    public static Season toEntity(SeasonRequestDTO dto) {
        if (dto == null) return null;
        Season s = new Season();
        s.setSeasonId(dto.getSeasonId());
        s.setTrophies(dto.getTrophies() == null ? 0 : dto.getTrophies());
        s.setBestTrophies(dto.getBestTrophies() == null ? 0 : dto.getBestTrophies());
        return s;
    }

    public static SeasonResponseDTO toDTO(Season season) {
        if (season == null) return null;
        return new SeasonResponseDTO(
                season.getSeasonId(),
                season.getTrophies(),
                season.getBestTrophies()
        );
    }
}

