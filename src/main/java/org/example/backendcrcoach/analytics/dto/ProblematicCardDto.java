package org.example.backendcrcoach.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProblematicCardDto {
    private Integer cardId;
    private String name;
    private Long appearances;
    private Double playerLossRate;
    private String iconUrl;
}

