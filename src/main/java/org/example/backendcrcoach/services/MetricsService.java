package org.example.backendcrcoach.services;

import org.example.backendcrcoach.domain.dto.MetricResponseDTO;
import org.example.backendcrcoach.domain.entities.*;
import org.example.backendcrcoach.mappers.MetricMapper;
import org.example.backendcrcoach.repositories.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class MetricsService {

    private final PlayerProfileRepository playerProfileRepository;
    private final SnapshotRepository snapshotRepository;
    private final BattleRepository battleRepository;
    private final GoalRepository goalRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public MetricsService(PlayerProfileRepository playerProfileRepository,
                          SnapshotRepository snapshotRepository,
                          BattleRepository battleRepository,
                          GoalRepository goalRepository,
                          NotificationRepository notificationRepository,
                          UserRepository userRepository) {
        this.playerProfileRepository = playerProfileRepository;
        this.snapshotRepository = snapshotRepository;
        this.battleRepository = battleRepository;
        this.goalRepository = goalRepository;
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

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
        List<Battle> recentBattles = battleRepository.findByPlayerProfileTagOrderByBattleTimeDesc(tag, PageRequest.of(0, battlesLimit));

        // battles.last24h and total battles
        List<Battle> allRecent = battleRepository.findByPlayerProfileTagOrderByBattleTimeDesc(tag, PageRequest.of(0, 200));
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

        return MetricMapper.toDtoFromData(profile, latest, change24, recentBattles, battlesLast24h, totalBattles, activeGoalsCount, mostAdvanced, unread);
    }
}

