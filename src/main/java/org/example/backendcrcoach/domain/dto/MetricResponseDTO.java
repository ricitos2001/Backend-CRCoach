package org.example.backendcrcoach.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MetricResponseDTO {
    private String playerTag;
    private String playerName;
    private LocalDateTime generatedAt;

    private TrophiesDto trophies;
    private ArenaDto arena;
    private LeagueDto league;
    private WinRateDto winRate;
    private LossRateDto lossRate;
    private StreakDto streak;
    private BattlesDto battles;
    private ActiveGoalsDto activeGoals;
    private Integer unreadNotifications;
    private Integer donations;
    private Integer chanceDonations24Hours;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrophiesDto {
        private Integer current;
        private Integer best;
        private Integer change24h;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ArenaDto {
        private Long id;
        private String name;
        private String iconUrl;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LeagueDto {
        private Integer number;
        private String name;
        private String iconUrl;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WinRateDto {
        private Double last25Battles;
        private Double last7Days;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LossRateDto {
        private Double last25Battles;
        private Double last7Days;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StreakDto {
        private Integer current;
        private String type; // WIN | LOSS | NONE
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BattlesDto {
        private Integer total;
        private Integer last24h;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActiveGoalsDto {
        private Integer count;
        private String nearestDeadline; // yyyy-MM-dd
        private MostAdvancedDto mostAdvanced;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MostAdvancedDto {
        private Long id;
        private String title;
        private Double progressPercent;
    }
}

