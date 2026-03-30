package org.example.backendcrcoach.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.backendcrcoach.domain.entities.Season;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LeagueStadisticRequestDTO {
    private Season currentSeason;
    private Season previousSeason;
    private Season bestSeason;
}


