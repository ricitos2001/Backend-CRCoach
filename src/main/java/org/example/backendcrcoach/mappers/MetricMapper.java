package org.example.backendcrcoach.mappers;

import org.example.backendcrcoach.domain.dto.MetricResponseDTO;
import org.example.backendcrcoach.domain.entities.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MetricMapper {

    public static MetricResponseDTO toDtoFromData(PlayerProfile profile,
                                                 Snapshot latestSnapshot,
                                                 Integer change24h,
                                                 List<Battle> recentBattles,
                                                 Integer battlesLast24h,
                                                 Integer totalBattles,
                                                 Integer activeGoalsCount,
                                                 Goal mostAdvancedGoal,
                                                 Integer unreadNotifications) {

        MetricResponseDTO dto = new MetricResponseDTO();
        dto.setPlayerTag(profile != null ? profile.getTag() : null);
        dto.setPlayerName(profile != null ? profile.getName() : null);
        dto.setGeneratedAt(java.time.LocalDateTime.now());

        MetricResponseDTO.TrophiesDto trophies = new MetricResponseDTO.TrophiesDto();
        trophies.setCurrent(latestSnapshot != null ? latestSnapshot.getTrophies() : (profile != null ? profile.getTrophies() : null));
        trophies.setBest(profile != null ? profile.getBestTrophies() : null);
        trophies.setChange24h(change24h);
        dto.setTrophies(trophies);

        if (profile != null && profile.getArena() != null) {
            MetricResponseDTO.ArenaDto arena = new MetricResponseDTO.ArenaDto();
            arena.setId(profile.getArena().getId());
            arena.setName(profile.getArena().getName());
            arena.setIconUrl(null);
            dto.setArena(arena);
        }

        if (profile != null && profile.getLeagueStatistics() != null && profile.getLeagueStatistics().getCurrentSeason() != null) {
            MetricResponseDTO.LeagueDto league = new MetricResponseDTO.LeagueDto();
            // Season entity in this project stores seasonId; use it as league name when present
            league.setNumber(null);
            league.setName(profile.getLeagueStatistics().getCurrentSeason().getSeasonId());
            league.setIconUrl(null);
            dto.setLeague(league);
        }

        // winRate calculations
        MetricResponseDTO.WinRateDto wr = new MetricResponseDTO.WinRateDto();
        wr.setLast25Battles(calculateWinRate(recentBattles));
        // last7Days is not calculated here (needs separate query), keep null
        wr.setLast7Days(null);
        dto.setWinRate(wr);

        // lossRate calculations (same logic as winRate but counting defeats)
        MetricResponseDTO.LossRateDto lr = new MetricResponseDTO.LossRateDto();
        lr.setLast25Battles(calculateLossRate(recentBattles));
        lr.setLast7Days(null);
        dto.setLossRate(lr);

        MetricResponseDTO.StreakDto st = calculateStreakDto(recentBattles);
        dto.setStreak(st);

        MetricResponseDTO.BattlesDto bd = new MetricResponseDTO.BattlesDto();
        bd.setTotal(totalBattles);
        bd.setLast24h(battlesLast24h);
        dto.setBattles(bd);

        MetricResponseDTO.ActiveGoalsDto ag = new MetricResponseDTO.ActiveGoalsDto();
        ag.setCount(activeGoalsCount);
        if (mostAdvancedGoal != null) {
            ag.setNearestDeadline(mostAdvancedGoal.getDeadline());
            MetricResponseDTO.MostAdvancedDto mad = new MetricResponseDTO.MostAdvancedDto();
            mad.setId(mostAdvancedGoal.getId());
            mad.setTitle(mostAdvancedGoal.getTitle());
            double prog = 0.0;
            if (mostAdvancedGoal.getTargetValue() != 0) {
                prog = (mostAdvancedGoal.getCurrentValue() / mostAdvancedGoal.getTargetValue()) * 100.0;
            }
            mad.setProgressPercent(prog);
            ag.setMostAdvanced(mad);
        }
        dto.setActiveGoals(ag);

        dto.setUnreadNotifications(unreadNotifications);

        return dto;
    }

    private static Double calculateWinRate(List<Battle> battles) {
        if (battles == null || battles.isEmpty()) return null;
        long wins = battles.stream().filter(b -> b.getTeam() != null && b.getTeam().getCrowns() != null && b.getOpponent() != null && b.getOpponent().getCrowns() != null && b.getTeam().getCrowns() > b.getOpponent().getCrowns()).count();
        return (double) wins / (double) battles.size();
    }

    private static Double calculateLossRate(List<Battle> battles) {
        if (battles == null || battles.isEmpty()) return null;
        long losses = battles.stream().filter(b -> {
            if (b == null) return false;
            if (b.getTeam() == null || b.getOpponent() == null) return false;
            Integer teamCrowns = b.getTeam().getCrowns();
            Integer oppCrowns = b.getOpponent().getCrowns();
            if (teamCrowns == null || oppCrowns == null) return false;
            return teamCrowns < oppCrowns;
        }).count();
        return (double) losses / (double) battles.size();
    }

    private static MetricResponseDTO.StreakDto calculateStreakDto(List<Battle> battles) {
        MetricResponseDTO.StreakDto s = new MetricResponseDTO.StreakDto();
        s.setCurrent(0);
        s.setType("NONE");
        if (battles == null || battles.isEmpty()) return s;

        // determine result of first battle
        Battle first = battles.get(0);
        Boolean firstWin = determineWin(first);
        if (firstWin == null) return s;

        int cnt = 0;
        for (Battle b : battles) {
            Boolean win = determineWin(b);
            if (win == null) break;
            if (win.equals(firstWin)) cnt++; else break;
        }
        s.setCurrent(cnt);
        s.setType(firstWin ? "WIN" : "LOSS");
        return s;
    }

    private static Boolean determineWin(Battle b) {
        if (b == null) return null;
        if (b.getTeam() == null || b.getOpponent() == null) return null;
        Integer teamCrowns = b.getTeam().getCrowns();
        Integer oppCrowns = b.getOpponent().getCrowns();
        if (teamCrowns == null || oppCrowns == null) return null;
        return teamCrowns > oppCrowns;
    }
}

