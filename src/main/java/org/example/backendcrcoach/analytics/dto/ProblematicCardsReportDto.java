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
public class ProblematicCardsReportDto {
    private String playerTag;
    private Long totalLosses;
    private List<ProblematicCardDto> problematicCards;
}

