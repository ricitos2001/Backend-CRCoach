package org.example.backendcrcoach.services;

import org.example.backendcrcoach.domain.dto.MetricResponseDTO;
import org.example.backendcrcoach.domain.entities.*;
import org.example.backendcrcoach.mappers.MetricMapper;
import org.example.backendcrcoach.repositories.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class MetricsService {

    private static final Logger log = LoggerFactory.getLogger(MetricsService.class);

    private final PlayerProfileRepository playerProfileRepository;
    private final SnapshotRepository snapshotRepository;
    private final BattleRepository battleRepository;
    private final GoalRepository goalRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final org.example.backendcrcoach.repositories.MetricRepository metricRepository;

    public MetricsService(PlayerProfileRepository playerProfileRepository,
                          SnapshotRepository snapshotRepository,
                          BattleRepository battleRepository,
                          GoalRepository goalRepository,
                          NotificationRepository notificationRepository,
                          UserRepository userRepository,
                          MetricRepository metricRepository) {
        this.playerProfileRepository = playerProfileRepository;
        this.snapshotRepository = snapshotRepository;
        this.battleRepository = battleRepository;
        this.goalRepository = goalRepository;
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.metricRepository = metricRepository;
    }

    @Transactional
    public MetricResponseDTO getPlayerSummary(String tag, Integer battlesLimit) {
        if (battlesLimit == null || battlesLimit <= 0) battlesLimit = 25;

        Optional<PlayerProfile> profileOpt = playerProfileRepository.findByTag(tag);
        if (profileOpt.isEmpty()) return null;
        PlayerProfile profile = profileOpt.get();

        // Último snapshot
        Optional<Snapshot> latestOpt = snapshotRepository.findTopByPlayerProfileTagOrderByCapturedAtDesc(tag);
        Snapshot latest = latestOpt.orElse(null);

        // snapshots en ventana 24h para calcular change24h
        LocalDateTime since = LocalDateTime.now().minus(24, ChronoUnit.HOURS);
        List<Snapshot> last24 = snapshotRepository.findByPlayerProfileTagAndCapturedAtAfterOrderByCapturedAtDesc(tag, since);
        Integer change24 = null;
        if (latest != null && !last24.isEmpty()) {
            Snapshot older = last24.get(last24.size() - 1); // el más antiguo dentro de la ventana
            change24 = latest.getTrophies() - older.getTrophies();
        }

        // batallas recientes
        List<Battle> recentBattles = battleRepository.findByTeamTagOrderByBattleTimeDesc(tag, PageRequest.of(0, battlesLimit));

        // battles.last24h and total battles
        List<Battle> allRecent = battleRepository.findByTeamTagOrderByBattleTimeDesc(tag, PageRequest.of(0, 200));
        int battlesLast24h = 0;
        for (Battle b : allRecent) {
            try {
                java.time.Instant inst = java.time.Instant.parse(b.getBattleTime());
                if (inst.isAfter(java.time.Instant.now().minus(24, ChronoUnit.HOURS))) battlesLast24h++;
            } catch (Exception e) {
                // ignore unparsable times
            }
        }

        int totalBattles = profile.getBattleCount() != null ? profile.getBattleCount() : 0;

        // active goals
        // resolve user email from playerTag if possible
        String userEmail = null;
        try {
            log.debug("Persistiendo metric provisional para tag {} trophies={}", profile.getTag(), latest != null ? latest.getTrophies() : profile.getTrophies());
            userEmail = userRepository.findByPlayerTag(profile.getTag()).map(User::getEmail).orElse(null);
        } catch (Exception ignored) {}

        List<Goal> activeGoals = Collections.emptyList();
        if (userEmail != null) {
            try {
                activeGoals = goalRepository.findByUserEmailAndStatus(userEmail, org.example.backendcrcoach.domain.enums.GoalStatus.IN_PROGRESS);
            } catch (Exception ignored) {
                activeGoals = Collections.emptyList();
            }
        }
        int activeGoalsCount = activeGoals != null ? activeGoals.size() : 0;
        Goal mostAdvanced = null;
        if (activeGoalsCount > 0) {
            mostAdvanced = activeGoals.stream().sorted((g1,g2) -> Double.compare((g2.getCurrentValue()/Math.max(1.0,g2.getTargetValue())), (g1.getCurrentValue()/Math.max(1.0,g1.getTargetValue())))).findFirst().orElse(null);
        }

        // unread notifications (using userEmail as profile.tag? Project stores userEmail in notification)
        int unread = 0;
        if (userEmail != null) unread = notificationRepository.countByUserEmailAndReadFalse(userEmail);

        // Persistir Metric y sus subentidades para mantener histórico
        try {
            Metric metric = new org.example.backendcrcoach.domain.entities.Metric();
            metric.setTag(profile.getTag());
            metric.setName(profile.getName());
            metric.setGeneratedAt(java.time.LocalDateTime.now());
            metric.setTrophies(latest != null ? latest.getTrophies() : profile.getTrophies());
            metric.setBestTrophies(profile.getBestTrophies());
            metric.setChangeTrophiesIn24h(change24);
            metric.setArena(profile.getArena());
            metric.setLeagueStatistics(profile.getLeagueStatistics());
            metric.setPlayerProfile(profile);

            // WinRate
            WinRate wr = new WinRate();
            wr.setLast25Battles(calculateWinRate(recentBattles));
            wr.setLast7Days(null);
            metric.setWinRate(wr);

            // Streak
            Streak st = calculateStreakEntity(recentBattles);
            metric.setStreak(st);

            // Battles stats
            Battles bs = new Battles();
            bs.setTotal(totalBattles);
            bs.setLast24h(battlesLast24h);
            metric.setBattles(bs);

            // ActiveGoals / MostAdvanced
            ActiveGoals ag = new ActiveGoals();
            ag.setCount(activeGoalsCount);
            if (mostAdvanced != null) {
                ag.setNearestDeadline(mostAdvanced.getDeadline());
                MostAdvanced ma = new MostAdvanced();
                ma.setTitle(mostAdvanced.getTitle());
                double prog = 0.0;
                if (mostAdvanced.getTargetValue() != null && mostAdvanced.getTargetValue() != 0) {
                    prog = (mostAdvanced.getCurrentValue() / mostAdvanced.getTargetValue()) * 100.0;
                }
                ma.setProgressPercent(prog);
                ag.setMostAdvanced(ma);
            }
            metric.setActiveGoals(ag);

            metric.setUnreadNotifications(unread);

            org.example.backendcrcoach.domain.entities.Metric saved = metricRepository.save(metric);
            log.info("Metric persisted id={} tag={}", saved.getId(), profile.getTag());
        } catch (Exception e) {
            // No detener la respuesta por fallo en persistencia; loguear
            log.error("Error persisting metric for tag {}: {}", profile != null ? profile.getTag() : "<null>", e.getMessage(), e);
        }

        return MetricMapper.toDtoFromData(profile, latest, change24, recentBattles, battlesLast24h, totalBattles, activeGoalsCount, mostAdvanced, unread);
    }

    private Double calculateWinRate(List<Battle> battles) {
        if (battles == null || battles.isEmpty()) return null;
        long wins = battles.stream().filter(b -> b.getTeam() != null && b.getTeam().getCrowns() != null && b.getOpponent() != null && b.getOpponent().getCrowns() != null && b.getTeam().getCrowns() > b.getOpponent().getCrowns()).count();
        return (double) wins / (double) battles.size();
    }

    private Streak calculateStreakEntity(List<Battle> battles) {
        Streak s = new Streak();
        s.setCurrent(0);
        s.setType("NONE");
        if (battles == null || battles.isEmpty()) return s;

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

    private Boolean determineWin(Battle b) {
        if (b == null) return null;
        if (b.getTeam() == null || b.getOpponent() == null) return null;
        Integer teamCrowns = b.getTeam().getCrowns();
        Integer oppCrowns = b.getOpponent().getCrowns();
        if (teamCrowns == null || oppCrowns == null) return null;
        return teamCrowns > oppCrowns;
    }
}

