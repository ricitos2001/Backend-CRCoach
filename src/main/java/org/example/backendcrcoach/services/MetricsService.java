package org.example.backendcrcoach.services;

import org.example.backendcrcoach.domain.dto.MetricResponseDTO;
import org.example.backendcrcoach.domain.entities.*;
import org.example.backendcrcoach.mappers.MetricMapper;
import org.example.backendcrcoach.repositories.*;
import org.example.backendcrcoach.domain.enums.GoalStatus;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
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
        // Calcular donaciones y cambio de donaciones en 24h
        Integer donations = latest != null ? latest.getDonations() : profile != null ? profile.getDonations() : null;

        // batallas recientes
        List<Battle> recentBattles = battleRepository.findByTeamTagOrderByBattleTimeDesc(tag, PageRequest.of(0, battlesLimit));

        // battles.last24h and total battles
        List<Battle> allRecent = battleRepository.findByTeamTagOrderByBattleTimeDesc(tag, PageRequest.of(0, 200));
        int battlesLast24h = 0;
        for (Battle b : allRecent) {
            try {
                Instant inst = parseBattleInstant(b.getBattleTime());
                if (inst != null && inst.isAfter(Instant.now().minus(24, ChronoUnit.HOURS))) battlesLast24h++;
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

        // Calcular win/loss last7Days usando batallas recientes (allRecent contiene hasta 200)
        Instant now = Instant.now();
        Instant sevenDaysAgo = now.minus(Duration.ofDays(7));
        long win7d = 0;
        long loss7d = 0;
        long total7d = 0;
        for (Battle b : allRecent) {
            try {
                Instant ts = parseBattleInstant(b.getBattleTime());
                if (ts == null) continue;
                if (ts.isBefore(sevenDaysAgo)) continue;
                total7d++;
                if (b.getTeam() != null && b.getOpponent() != null && b.getTeam().getCrowns() != null && b.getOpponent().getCrowns() != null) {
                    if (b.getTeam().getCrowns() > b.getOpponent().getCrowns()) win7d++; else if (b.getTeam().getCrowns() < b.getOpponent().getCrowns()) loss7d++;
                }
            } catch (Exception ignore) {
                // skip unparsable battle times
            }
        }
        Double winRate7d = total7d == 0 ? null : Math.round(((double) win7d / total7d) * 10000.0) / 10000.0;
        Double lossRate7d = total7d == 0 ? null : Math.round(((double) loss7d / total7d) * 10000.0) / 10000.0;

        // Persistir Metric y sus subentidades para mantener histórico
        try {
            Metric metric = new Metric();
            metric.setTag(profile.getTag());
            metric.setName(profile.getName());
            metric.setGeneratedAt(LocalDateTime.now());
            metric.setTrophies(latest != null ? latest.getTrophies() : profile.getTrophies());
            metric.setBestTrophies(profile.getBestTrophies());
            metric.setChangeTrophiesIn24h(change24);
            metric.setArena(profile.getArena());
            metric.setLeagueStatistics(profile.getLeagueStatistics());
            metric.setPlayerProfile(profile);

            // WinRate
            WinRate wr = new WinRate();
            wr.setLast25Battles(calculateWinRate(recentBattles));
            wr.setLast7Days(winRate7d);
            metric.setWinRate(wr);

            // LossRate
            LossRate lr = new LossRate();
            lr.setLast25Battles(calculateLossRate(recentBattles));
            lr.setLast7Days(lossRate7d);
            metric.setLossRate(lr);

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
            metric.setDonations(donations);

            // Actualizar los objetivos activos del usuario con los valores calculados en esta métrica.
            // Esto asegura que `Goal.currentValue` refleje el estado real (trofeos, racha, donaciones, batallas, winRate).
            if (userEmail != null && activeGoals != null && !activeGoals.isEmpty()) {
                for (Goal g : activeGoals) {
                    try {
                        if (g.getMetricType() == null) continue;
                        String mt = g.getMetricType().toLowerCase();
                        Double newVal = null;
                        switch (mt) {
                            case "trophies":
                            case "trofeos":
                                newVal = metric.getTrophies() != null ? Double.valueOf(metric.getTrophies()) : null;
                                break;
                            case "winrate":
                            case "win_rate":
                            case "porcentaje_victorias":
                            case "porcentaje":
                                if (metric.getWinRate() != null) {
                                    if (metric.getWinRate().getLast7Days() != null) newVal = metric.getWinRate().getLast7Days() * 100.0;
                                    else if (metric.getWinRate().getLast25Battles() != null) newVal = metric.getWinRate().getLast25Battles() * 100.0;
                                }
                                break;
                            case "streak":
                            case "racha":
                                if (metric.getStreak() != null && metric.getStreak().getCurrent() != null) newVal = Double.valueOf(metric.getStreak().getCurrent());
                                break;
                            case "donations":
                            case "donaciones":
                                if (metric.getDonations() != null) newVal = Double.valueOf(metric.getDonations());
                                break;
                            case "battles":
                            case "batallas":
                                if (metric.getBattles() != null && metric.getBattles().getTotal() != null) newVal = Double.valueOf(metric.getBattles().getTotal());
                                break;
                            default:
                                // Si el metricType no se reconoce, intentar no modificar currentValue
                                newVal = null;
                        }
                        if (newVal != null) {
                            g.setCurrentValue(newVal);
                            // Marcar completado si alcanza o supera el target
                            if (g.getTargetValue() != null && newVal >= g.getTargetValue()) {
                                g.setStatus(GoalStatus.COMPLETED);
                            }
                            goalRepository.save(g);
                        }
                    } catch (Exception ignored) {
                        throw new RuntimeException("Error actualizando goal id " + g.getId() + " para user " + userEmail + ": " + ignored.getMessage(), ignored);
                    }
                }
            }

            // Si la metric contiene leagueStatistics con id, intentar buscar una métrica
            // existente vinculada a esa leagueStatistics y actualizarla en lugar de insertar.
            Metric saved;
            if (metric.getLeagueStatistics() != null && metric.getLeagueStatistics().getId() != null) {
                // Intentar localizar por leagueStatistics.id primero
                Optional<Metric> existing = metricRepository.findByLeagueStatisticsId(metric.getLeagueStatistics().getId());
                // Si no existe por leagueStatistics.id, intentar localizar por tag (fallback)
                if (existing.isEmpty()) {
                    existing = metricRepository.findTopByTag(metric.getTag());
                }

                if (existing.isPresent()) {
                    Metric existingMetric = existing.get();
                    // Actualizar campos simples
                    existingMetric.setTag(metric.getTag());
                    existingMetric.setName(metric.getName());
                    existingMetric.setGeneratedAt(metric.getGeneratedAt());
                    existingMetric.setTrophies(metric.getTrophies());
                    existingMetric.setBestTrophies(metric.getBestTrophies());
                    existingMetric.setChangeTrophiesIn24h(metric.getChangeTrophiesIn24h());
                    existingMetric.setPlayerProfile(metric.getPlayerProfile());
                    existingMetric.setUnreadNotifications(metric.getUnreadNotifications());
                    existingMetric.setDonations(metric.getDonations());

                    // Arena: actualizar campos existentes para evitar crear filas nuevas
                    if (metric.getArena() != null) {
                        if (existingMetric.getArena() != null) {
                            existingMetric.getArena().setName(metric.getArena().getName());
                            existingMetric.getArena().setRawName(metric.getArena().getRawName());
                        } else {
                            existingMetric.setArena(metric.getArena());
                        }
                    }

                    // LeagueStadistic: evitar crear nueva entidad si ya existe
                    if (metric.getLeagueStatistics() != null) {
                        if (existingMetric.getLeagueStatistics() != null) {
                            existingMetric.getLeagueStatistics().setCurrentSeason(metric.getLeagueStatistics().getCurrentSeason());
                            existingMetric.getLeagueStatistics().setPreviousSeason(metric.getLeagueStatistics().getPreviousSeason());
                            existingMetric.getLeagueStatistics().setBestSeason(metric.getLeagueStatistics().getBestSeason());
                        } else {
                            existingMetric.setLeagueStatistics(metric.getLeagueStatistics());
                        }
                    }

                    // WinRate: actualizar campos internos si existe
                    if (metric.getWinRate() != null) {
                        if (existingMetric.getWinRate() != null) {
                            existingMetric.getWinRate().setLast25Battles(metric.getWinRate().getLast25Battles());
                            existingMetric.getWinRate().setLast7Days(metric.getWinRate().getLast7Days());
                        } else {
                            existingMetric.setWinRate(metric.getWinRate());
                        }
                    }

                    // LossRate
                    if (metric.getLossRate() != null) {
                        if (existingMetric.getLossRate() != null) {
                            existingMetric.getLossRate().setLast25Battles(metric.getLossRate().getLast25Battles());
                            existingMetric.getLossRate().setLast7Days(metric.getLossRate().getLast7Days());
                        } else {
                            existingMetric.setLossRate(metric.getLossRate());
                        }
                    }

                    // Streak
                    if (metric.getStreak() != null) {
                        if (existingMetric.getStreak() != null) {
                            existingMetric.getStreak().setCurrent(metric.getStreak().getCurrent());
                            existingMetric.getStreak().setType(metric.getStreak().getType());
                        } else {
                            existingMetric.setStreak(metric.getStreak());
                        }
                    }

                    // Battles
                    if (metric.getBattles() != null) {
                        if (existingMetric.getBattles() != null) {
                            existingMetric.getBattles().setTotal(metric.getBattles().getTotal());
                            existingMetric.getBattles().setLast24h(metric.getBattles().getLast24h());
                        } else {
                            existingMetric.setBattles(metric.getBattles());
                        }
                    }

                    // ActiveGoals / MostAdvanced
                    if (metric.getActiveGoals() != null) {
                        if (existingMetric.getActiveGoals() != null) {
                            existingMetric.getActiveGoals().setCount(metric.getActiveGoals().getCount());
                            existingMetric.getActiveGoals().setNearestDeadline(metric.getActiveGoals().getNearestDeadline());
                            if (metric.getActiveGoals().getMostAdvanced() != null) {
                                if (existingMetric.getActiveGoals().getMostAdvanced() != null) {
                                    existingMetric.getActiveGoals().getMostAdvanced().setTitle(metric.getActiveGoals().getMostAdvanced().getTitle());
                                    existingMetric.getActiveGoals().getMostAdvanced().setProgressPercent(metric.getActiveGoals().getMostAdvanced().getProgressPercent());
                                } else {
                                    existingMetric.getActiveGoals().setMostAdvanced(metric.getActiveGoals().getMostAdvanced());
                                }
                            }
                        } else {
                            existingMetric.setActiveGoals(metric.getActiveGoals());
                        }
                    }

                    saved = metricRepository.save(existingMetric);
                } else {
                    // No se encontró métrica existente: persistir nueva
                    saved = metricRepository.save(metric);
                }
            } else {
                // No hay leagueStatistics.id; intentar localizar por tag como fallback
                Optional<Metric> existingByTag = metricRepository.findTopByTag(metric.getTag());
                if (existingByTag.isPresent()) {
                    Metric existingMetric = existingByTag.get();
                    // Actualizar solo los campos necesarios (evitar crear nuevas entidades ActiveGoals)
                    existingMetric.setTag(metric.getTag());
                    existingMetric.setName(metric.getName());
                    existingMetric.setGeneratedAt(metric.getGeneratedAt());
                    existingMetric.setTrophies(metric.getTrophies());
                    existingMetric.setBestTrophies(metric.getBestTrophies());
                    existingMetric.setChangeTrophiesIn24h(metric.getChangeTrophiesIn24h());
                    existingMetric.setPlayerProfile(metric.getPlayerProfile());
                    existingMetric.setUnreadNotifications(metric.getUnreadNotifications());
                    existingMetric.setDonations(metric.getDonations());

                    // Actualizar ActiveGoals si ya existe para evitar insertar nueva fila
                    if (metric.getActiveGoals() != null) {
                        if (existingMetric.getActiveGoals() != null) {
                            existingMetric.getActiveGoals().setCount(metric.getActiveGoals().getCount());
                            existingMetric.getActiveGoals().setNearestDeadline(metric.getActiveGoals().getNearestDeadline());
                            if (metric.getActiveGoals().getMostAdvanced() != null) {
                                if (existingMetric.getActiveGoals().getMostAdvanced() != null) {
                                    existingMetric.getActiveGoals().getMostAdvanced().setTitle(metric.getActiveGoals().getMostAdvanced().getTitle());
                                    existingMetric.getActiveGoals().getMostAdvanced().setProgressPercent(metric.getActiveGoals().getMostAdvanced().getProgressPercent());
                                } else {
                                    existingMetric.getActiveGoals().setMostAdvanced(metric.getActiveGoals().getMostAdvanced());
                                }
                            }
                        } else {
                            existingMetric.setActiveGoals(metric.getActiveGoals());
                        }
                    }

                    saved = metricRepository.save(existingMetric);
                } else {
                    saved = metricRepository.save(metric);
                }
            }
            log.info("Metric persisted id={} tag={}", saved.getId(), profile.getTag());
        } catch (Exception e) {
            // No detener la respuesta por fallo en persistencia; loguear
            log.error("Error persisting metric for tag {}: {}", profile != null ? profile.getTag() : "<null>", e.getMessage(), e);
        }

        return MetricMapper.toDtoFromData(profile, latest, change24, recentBattles, battlesLast24h, totalBattles, activeGoalsCount, mostAdvanced, unread, winRate7d, lossRate7d, donations);
    }

    private Double calculateWinRate(List<Battle> battles) {
        if (battles == null || battles.isEmpty()) return null;
        long wins = battles.stream().filter(b -> b.getTeam() != null && b.getTeam().getCrowns() != null && b.getOpponent() != null && b.getOpponent().getCrowns() != null && b.getTeam().getCrowns() > b.getOpponent().getCrowns()).count();
        double raw = (double) wins / (double) battles.size();
        return Math.round(raw * 10000.0) / 10000.0;
    }

    private Double calculateLossRate(List<Battle> battles) {
        if (battles == null || battles.isEmpty()) return null;
        long losses = battles.stream().filter(b -> b.getTeam() != null && b.getTeam().getCrowns() != null && b.getOpponent() != null && b.getOpponent().getCrowns() != null && b.getTeam().getCrowns() < b.getOpponent().getCrowns()).count();
        double raw = (double) losses / (double) battles.size();
        return Math.round(raw * 10000.0) / 10000.0;
    }

    /**
     * Intenta parsear la fecha de batalla en varios formatos conocidos.
     * Devuelve null si no pudo parsearse.
     */
    private Instant parseBattleInstant(String s) {
        if (s == null) return null;
        try {
            return Instant.parse(s);
        } catch (Exception ignored) {}

        // formato tipo 20260416T155547.000Z o 20260416T155547Z
        try {
            DateTimeFormatter fmtMillis = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss.SSS'Z'").withZone(ZoneOffset.UTC);
            return Instant.from(fmtMillis.parse(s));
        } catch (Exception ignored) {}

        try {
             DateTimeFormatter fmtNoMillis = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'").withZone(ZoneOffset.UTC);
            return Instant.from(fmtNoMillis.parse(s));
        } catch (Exception ignored) {}

        try {
            // Transformar 20260416T155547.000Z -> 2026-04-16T15:55:47.000Z
            if (s.matches("\\d{8}T\\d{6}.*")) {
                String date = s.substring(0, 8);
                String timeAndRest = s.substring(9);
                String time;
                String rest = "";
                int dotIdx = timeAndRest.indexOf('.');
                if (dotIdx > 0) {
                    time = timeAndRest.substring(0, dotIdx);
                    rest = timeAndRest.substring(dotIdx);
                } else {
                    time = timeAndRest;
                }
                String normalized = date.substring(0,4) + "-" + date.substring(4,6) + "-" + date.substring(6,8) + "T" + time.substring(0,2) + ":" + time.substring(2,4) + ":" + time.substring(4,6) + rest;
                return Instant.parse(normalized);
            }
        } catch (Exception ignored) {}

        return null;
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

