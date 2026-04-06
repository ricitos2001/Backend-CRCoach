package org.example.backendcrcoach.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class WeaknessReportDto {
    private String playerTag;
    private Long totalBattles;
    private String periodFrom;
    private String periodTo;
    private List<ArchetypeStatDto> byArchetype;
    private String weakestArchetype;
    private String strongestArchetype;
}

