package org.example.backendcrcoach.analytics;

import jakarta.transaction.Transactional;
import org.example.backendcrcoach.analytics.dto.*;
import org.example.backendcrcoach.domain.entities.Battle;
import org.example.backendcrcoach.domain.entities.PlayerCard;
import org.example.backendcrcoach.domain.entities.PlayerProfile;
import org.example.backendcrcoach.repositories.PlayerProfileRepository;
import org.example.backendcrcoach.repositories.BattleRepository;
import org.example.backendcrcoach.repositories.WeaknessReportRepository;
import org.example.backendcrcoach.repositories.ProblematicCardsReportRepository;
import org.example.backendcrcoach.repositories.PlayerSummaryReportRepository;
import org.example.backendcrcoach.domain.entities.WeaknessReport;
import org.example.backendcrcoach.domain.entities.ProblematicCardsReport;
import org.example.backendcrcoach.domain.entities.PlayerSummaryReport;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class AnalyticsService {

    private final BattleRepository battleRepository;
    private final PlayerProfileRepository playerProfileRepository;
    private final WeaknessReportRepository weaknessReportRepository;
    private final ProblematicCardsReportRepository problematicCardsReportRepository;
    private final PlayerSummaryReportRepository playerSummaryReportRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AnalyticsService(BattleRepository battleRepository,
                            PlayerProfileRepository playerProfileRepository,
                            WeaknessReportRepository weaknessReportRepository,
                            ProblematicCardsReportRepository problematicCardsReportRepository,
                            PlayerSummaryReportRepository playerSummaryReportRepository) {
        this.battleRepository = battleRepository;
        this.playerProfileRepository = playerProfileRepository;
        this.weaknessReportRepository = weaknessReportRepository;
        this.problematicCardsReportRepository = problematicCardsReportRepository;
        this.playerSummaryReportRepository = playerSummaryReportRepository;
    }

    /**
     * Genera el informe de debilidades por arquetipo para el jugador (tag debe incluir '#').
     */
    public WeaknessReportDto getWeaknesses(String tag, String gameMode, String from, String to, Integer minBattles) {
        Instant fromInst = parseNullableInstant(from);
        Instant toInst = parseNullableInstant(to);
        List<Battle> battles;
        if (fromInst != null || toInst != null) {
            battles = battleRepository.findAll(buildSpecForTagAndFilters(tag, gameMode, fromInst, toInst));
        } else {
            battles = battleRepository.findByTeamTagWithFilters(tag, gameMode);
        }

        WeaknessReportDto report = new WeaknessReportDto();
        report.setPlayerTag(tag);
        report.setTotalBattles((long) battles.size());
        report.setPeriodFrom(from);
        report.setPeriodTo(to);

        Map<Archetype, long[]> counts = new HashMap<>(); // archetype -> [total, wins]

        for (Battle b : battles) {
            Archetype arch = Archetype.UNKNOWN;
            // Determinar cuál es el rival respecto al tag solicitado
            boolean playerIsTeam = b.getTeam() != null && tagEqualsNormalized(tag, b.getTeam().getTag());
            boolean playerIsOpponent = b.getOpponent() != null && tagEqualsNormalized(tag, b.getOpponent().getTag());
            // rivalDeck = the other side's deck
            if (playerIsTeam) {
                if (b.getOpponent() != null && b.getOpponent().getPlayerDeck() != null && b.getOpponent().getPlayerDeck().getArchetype() != null) {
                    arch = b.getOpponent().getPlayerDeck().getArchetype();
                }
            } else if (playerIsOpponent) {
                if (b.getTeam() != null && b.getTeam().getPlayerDeck() != null && b.getTeam().getPlayerDeck().getArchetype() != null) {
                    arch = b.getTeam().getPlayerDeck().getArchetype();
                }
            } else {
                // Si el tag no coincide con ninguna de las dos caras, intentar usar opponent por defecto
                if (b.getOpponent() != null && b.getOpponent().getPlayerDeck() != null && b.getOpponent().getPlayerDeck().getArchetype() != null) {
                    arch = b.getOpponent().getPlayerDeck().getArchetype();
                }
            }

            boolean win = inferWinFromBattle(b, tag);

            long[] arr = counts.computeIfAbsent(arch, k -> new long[2]);
            arr[0]++;
            if (win) arr[1]++;
        }

        List<ArchetypeStatDto> stats = new ArrayList<>();
        for (Map.Entry<Archetype, long[]> e : counts.entrySet()) {
            long total = e.getValue()[0];
            long wins = e.getValue()[1];
            if (minBattles != null && total < minBattles) continue;
            ArchetypeStatDto dto = new ArchetypeStatDto();
            dto.setArchetype(e.getKey());
            dto.setBattles(total);
            dto.setWins(wins);
            dto.setLosses(total - wins);
            double winRate = total == 0 ? 0.0 : ((double) wins) / total;
            dto.setWinRate(Math.round(winRate * 10000.0) / 10000.0);
            dto.setLabel(labelForWinRate(winRate));
            stats.add(dto);
        }

        // Ordenar ascendente por winRate (peores primero)
        stats.sort(Comparator.comparing(ArchetypeStatDto::getWinRate));

        report.setByArchetype(stats);

        if (!stats.isEmpty()) {
            report.setWeakestArchetype(stats.get(0).getArchetype().name());
            report.setStrongestArchetype(stats.get(stats.size() - 1).getArchetype().name());
        }

        return report;
    }

    /**
     * Devuelve las cartas problemáticas (las que más aparecen en derrotas del jugador).
     */
    public List<ProblematicCardDto> getProblematicCards(String tag, String gameMode, String from, String to, Integer limit, Integer minAppearances) {
        Instant fromInst = parseNullableInstant(from);
        Instant toInst = parseNullableInstant(to);
        List<Battle> battles;
        if (fromInst != null || toInst != null) {
            battles = battleRepository.findAll(buildSpecForTagAndFilters(tag, gameMode, fromInst, toInst));
        } else {
            battles = battleRepository.findByTeamTagWithFilters(tag, gameMode);
        }

        long totalLosses = battles.stream().filter(b -> !inferWinFromBattle(b, tag)).count();

        Map<Integer, Long> counts = new HashMap<>();
        Map<Integer, PlayerCard> sampleCard = new HashMap<>();

        for (Battle b : battles) {
            if (inferWinFromBattle(b, tag)) continue; // solo derrotas
            // Determinar las cartas del rival en esta batalla (según side)
            boolean playerIsTeam = b.getTeam() != null && tagEqualsNormalized(tag, b.getTeam().getTag());
            boolean playerIsOpponent = b.getOpponent() != null && tagEqualsNormalized(tag, b.getOpponent().getTag());
            List<PlayerCard> rivalCards = null;
            if (playerIsTeam) {
                if (b.getOpponent() != null && b.getOpponent().getPlayerDeck() != null) rivalCards = b.getOpponent().getPlayerDeck().getPlayerCards();
            } else if (playerIsOpponent) {
                if (b.getTeam() != null && b.getTeam().getPlayerDeck() != null) rivalCards = b.getTeam().getPlayerDeck().getPlayerCards();
            } else {
                // fallback: use opponent
                if (b.getOpponent() != null && b.getOpponent().getPlayerDeck() != null) rivalCards = b.getOpponent().getPlayerDeck().getPlayerCards();
            }

            if (rivalCards == null) continue;
            for (PlayerCard pc : rivalCards) {
                if (pc == null || pc.getCardId() == null) continue;
                counts.merge(pc.getCardId(), 1L, Long::sum);
                sampleCard.putIfAbsent(pc.getCardId(), pc);
            }
        }

        List<ProblematicCardDto> list = counts.entrySet().stream()
                .filter(e -> minAppearances == null || e.getValue() >= minAppearances)
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(limit == null ? 10 : limit)
                .map(e -> {
                    PlayerCard pc = sampleCard.get(e.getKey());
                    ProblematicCardDto dto = new ProblematicCardDto();
                    dto.setCardId(e.getKey());
                    dto.setName(pc != null ? pc.getName() : null);
                    dto.setAppearances(e.getValue());
                    dto.setPlayerLossRate(totalLosses == 0 ? 0.0 : ((double) e.getValue()) / totalLosses);
                    dto.setIconUrl(pc != null && pc.getIconUrl() != null ? pc.getIconUrl().getMedium() : null);
                    return dto;
                })
                .collect(Collectors.toList());

        return list;
    }

    public long getTotalLossesForFilter(String tag, String gameMode, String from, String to) {
        Instant fromInst = parseNullableInstant(from);
        Instant toInst = parseNullableInstant(to);
        List<Battle> battles;
        if (fromInst != null || toInst != null) {
            battles = battleRepository.findAll(buildSpecForTagAndFilters(tag, gameMode, fromInst, toInst));
        } else {
            battles = battleRepository.findByTeamTagWithFilters(tag, gameMode);
        }
        return battles.stream().filter(b -> !inferWinFromBattle(b, tag)).count();
    }

    // Construye una Specification dinámica para evitar pasar parámetros NULL sin tipo al SQL generado
    private org.springframework.data.jpa.domain.Specification<Battle> buildSpecForTagAndFilters(String tag, String gameMode, Instant fromInst, Instant toInst) {
        return (root, query, cb) -> {
            jakarta.persistence.criteria.Join<Object, Object> teamJoin = root.join("team", jakarta.persistence.criteria.JoinType.LEFT);
            jakarta.persistence.criteria.Join<Object, Object> oppJoin = root.join("opponent", jakarta.persistence.criteria.JoinType.LEFT);

            java.util.List<jakarta.persistence.criteria.Predicate> preds = new java.util.ArrayList<>();

            // Prepare normalized tag forms for comparison (with and without '#')
            String noHash = normalizeTagForDbComparisons(tag);
            String withHash = noHash != null ? ("#" + noHash) : null;
            jakarta.persistence.criteria.Expression<String> teamTagExpr = cb.upper(teamJoin.get("tag"));
            jakarta.persistence.criteria.Expression<String> oppTagExpr = cb.upper(oppJoin.get("tag"));
            java.util.List<jakarta.persistence.criteria.Predicate> tagOrs = new java.util.ArrayList<>();
            if (withHash != null) tagOrs.add(cb.equal(teamTagExpr, withHash));
            if (noHash != null) tagOrs.add(cb.equal(teamTagExpr, noHash));
            if (withHash != null) tagOrs.add(cb.equal(oppTagExpr, withHash));
            if (noHash != null) tagOrs.add(cb.equal(oppTagExpr, noHash));
            jakarta.persistence.criteria.Predicate tagPred = cb.or(tagOrs.toArray(new jakarta.persistence.criteria.Predicate[0]));
            preds.add(tagPred);

            if (gameMode != null) {
                preds.add(cb.equal(root.get("gameMode").get("name"), gameMode));
            }
            if (fromInst != null) {
                preds.add(cb.greaterThanOrEqualTo(root.get("battleTime"), formatInstantAsDbString(fromInst)));
            }
            if (toInst != null) {
                preds.add(cb.lessThanOrEqualTo(root.get("battleTime"), formatInstantAsDbString(toInst)));
            }

            return cb.and(preds.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }

    /**
     * Resumen del jugador con métricas agregadas (trophies, win rates, streak, avg elixir)
     */
    public PlayerSummaryDto getPlayerSummary(String tag, String gameMode, String from, String to) {
        PlayerSummaryDto dto = new PlayerSummaryDto();
        dto.setPlayerTag(tag);

        // Trophies desde PlayerProfile snapshot si existe
        PlayerProfile profile = playerProfileRepository.findByTag(tag).orElse(null);
        dto.setTrophies(profile != null ? profile.getTrophies() : null);

        // Todas las batallas del jugador con filtros opcionales
        Instant fromInst = parseNullableInstant(from);
        Instant toInst = parseNullableInstant(to);
        List<Battle> filteredBattles;
        // Usar Specification dinámica cuando se pase algún bound de fecha. Evita pasar NULL tipados
        // a métodos JPQL que provoquen errores en Postgres.
        if (fromInst != null || toInst != null) {
            filteredBattles = battleRepository.findAll(buildSpecForTagAndFilters(tag, gameMode, fromInst, toInst));
        } else {
            filteredBattles = battleRepository.findByTeamTagWithFilters(tag, gameMode);
        }
        dto.setTotalBattles((long) filteredBattles.size());

        // Win rate last 25: ordenar por battleTime (desc) y tomar 25
        // Ordenar por timestamp de batalla: parsear siempre el campo battleTime (string)
        List<Battle> recentSorted = new ArrayList<>(filteredBattles);
        recentSorted.sort((a, b) -> {
            Instant ta = parseNullableInstant(a.getBattleTime());
            Instant tb = parseNullableInstant(b.getBattleTime());
            if (ta == null) ta = Instant.EPOCH;
            if (tb == null) tb = Instant.EPOCH;
            return tb.compareTo(ta);
        });
        List<Battle> recent25 = recentSorted.stream().limit(25).collect(Collectors.toList());
        long wins25 = recent25.stream().filter(b -> inferWinFromBattle(b, tag)).count();
        dto.setWinRateLast25(recent25.isEmpty() ? null : Math.round(((double) wins25 / recent25.size()) * 10000.0) / 10000.0);

        // Win rate last 7 days
        Instant now = Instant.now();
        Instant sevenDaysAgo = now.minus(Duration.ofDays(7));
        long wins7d = 0;
        long total7d = 0;
        for (Battle b : filteredBattles) {
            Instant ts = parseNullableInstant(b.getBattleTime());
            if (ts == null) continue;
            if (ts.isBefore(sevenDaysAgo)) continue;
            total7d++;
            if (inferWinFromBattle(b, tag)) wins7d++;
        }
        dto.setWinRateLast7d(total7d == 0 ? null : Math.round(((double) wins7d / total7d) * 10000.0) / 10000.0);

        // Current streak: count consecutive wins from most recent
        int streak = 0;
        for (Battle b : recent25) {
            boolean win = inferWinFromBattle(b, tag);
            if (win) streak++; else break;
        }
        dto.setCurrentStreak(streak);

        // Weakest / strongest archetype using existing weaknesses function (aplicar mismos filtros)
        WeaknessReportDto weaknesses = getWeaknesses(tag, gameMode, from, to, 1);
        dto.setWeakestArchetype(weaknesses.getWeakestArchetype());
        dto.setStrongestArchetype(weaknesses.getStrongestArchetype());

        // avg elixir last deck: take most recent battle in recentSorted and compute avg elixir from player's side deck
        Double avgElixir = null;
        if (!recentSorted.isEmpty()) {
            Battle last = recentSorted.get(0);
            // Determine which side is player
            boolean playerIsTeam = last.getTeam() != null && tagEqualsNormalized(tag, last.getTeam().getTag());
            if (playerIsTeam && last.getTeam().getPlayerDeck() != null && last.getTeam().getPlayerDeck().getPlayerCards() != null) {
                java.util.List<PlayerCard> pcs = last.getTeam().getPlayerDeck().getPlayerCards();
                double avg = pcs.stream().filter(p -> p != null && p.getElixirCost() != null).mapToInt(PlayerCard::getElixirCost).average().orElse(Double.NaN);
                if (!Double.isNaN(avg)) avgElixir = Math.round(avg * 100.0) / 100.0;
            } else if (!playerIsTeam && last.getOpponent() != null && last.getOpponent().getPlayerDeck() != null && last.getOpponent().getPlayerDeck().getPlayerCards() != null) {
                java.util.List<PlayerCard> pcs = last.getOpponent().getPlayerDeck().getPlayerCards();
                double avg = pcs.stream().filter(p -> p != null && p.getElixirCost() != null).mapToInt(PlayerCard::getElixirCost).average().orElse(Double.NaN);
                if (!Double.isNaN(avg)) avgElixir = Math.round(avg * 100.0) / 100.0;
            }
        }
        dto.setAvgElixirLastDeck(avgElixir);

        return dto;
    }

    // Persistencia de informes: convierten DTOs a entidades simples y las guardan
    public void saveWeaknessReport(WeaknessReportDto dto) {
        if (dto == null) return;
        WeaknessReport e = WeaknessReport.builder()
                .playerTag(dto.getPlayerTag())
                .totalBattles(dto.getTotalBattles())
                .periodFrom(dto.getPeriodFrom())
                .periodTo(dto.getPeriodTo())
                .weakestArchetype(dto.getWeakestArchetype())
                .strongestArchetype(dto.getStrongestArchetype())
                .createdAt(Instant.now())
                .build();
        try {
            if (dto.getByArchetype() != null) e.setByArchetypeJson(objectMapper.writeValueAsString(dto.getByArchetype()));
        } catch (JsonProcessingException ex) {
            // fallback: store toString
            e.setByArchetypeJson(dto.getByArchetype().toString());
        }
        weaknessReportRepository.save(e);
    }

    public void saveProblematicCardsReport(ProblematicCardsReportDto dto) {
        if (dto == null) return;
        ProblematicCardsReport e = ProblematicCardsReport.builder()
                .playerTag(dto.getPlayerTag())
                .totalLosses(dto.getTotalLosses())
                .createdAt(Instant.now())
                .build();
        try {
            if (dto.getProblematicCards() != null) e.setProblematicCardsJson(objectMapper.writeValueAsString(dto.getProblematicCards()));
        } catch (JsonProcessingException ex) {
            e.setProblematicCardsJson(dto.getProblematicCards().toString());
        }
        problematicCardsReportRepository.save(e);
    }

    public void savePlayerSummary(PlayerSummaryDto dto) {
        if (dto == null) return;
        PlayerSummaryReport e = PlayerSummaryReport.builder()
                .playerTag(dto.getPlayerTag())
                .trophies(dto.getTrophies())
                .winRateLast25(dto.getWinRateLast25())
                .winRateLast7d(dto.getWinRateLast7d())
                .currentStreak(dto.getCurrentStreak())
                .totalBattles(dto.getTotalBattles())
                .weakestArchetype(dto.getWeakestArchetype())
                .strongestArchetype(dto.getStrongestArchetype())
                .avgElixirLastDeck(dto.getAvgElixirLastDeck())
                .createdAt(Instant.now())
                .build();
        playerSummaryReportRepository.save(e);
    }

    private boolean inferWinFromBattle(Battle b, String playerTag) {
        if (b == null) return false;
        boolean playerIsTeam = b.getTeam() != null && tagEqualsNormalized(playerTag, b.getTeam().getTag());
        boolean playerIsOpponent = b.getOpponent() != null && tagEqualsNormalized(playerTag, b.getOpponent().getTag());
        if (!playerIsTeam && !playerIsOpponent) return false;

        // 1) Preferir trophyChange del lado del jugador; si no existe, intentar inferir a partir del trophyChange del contrario
        Integer playerTrophyChange = playerIsTeam ? (b.getTeam() != null ? b.getTeam().getTrophyChange() : null) : (b.getOpponent() != null ? b.getOpponent().getTrophyChange() : null);
        Integer otherTrophyChange = playerIsTeam ? (b.getOpponent() != null ? b.getOpponent().getTrophyChange() : null) : (b.getTeam() != null ? b.getTeam().getTrophyChange() : null);

        if (playerTrophyChange != null) return playerTrophyChange > 0;
        if (otherTrophyChange != null) return otherTrophyChange < 0; // si el contrario pierde trofeos (<0), el jugador ganó

        // 2) Si trophyChange no está en ninguno de los dos, usar crowns comparando lado del jugador vs contrario
        Integer playerCrowns = playerIsTeam ? (b.getTeam() != null ? b.getTeam().getCrowns() : null) : (b.getOpponent() != null ? b.getOpponent().getCrowns() : null);
        Integer otherCrowns = playerIsTeam ? (b.getOpponent() != null ? b.getOpponent().getCrowns() : null) : (b.getTeam() != null ? b.getTeam().getCrowns() : null);
        if (playerCrowns != null && otherCrowns != null) return playerCrowns > otherCrowns;

        return false;
    }

    private String labelForWinRate(double winRate) {
        if (winRate < 0.40) return "Debilidad crítica";
        if (winRate < 0.50) return "Debilidad";
        if (winRate >= 0.65) return "Fortaleza";
        return "Neutral";
    }

    private Instant parseNullableInstant(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return Instant.parse(s);
        } catch (Exception e) {
            try {
                return java.time.OffsetDateTime.parse(s).toInstant();
            } catch (Exception ex) {
                // Try compact format like 20260405T153200.000Z
                try {
                    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss.SSSX");
                    OffsetDateTime odt = OffsetDateTime.parse(s, fmt);
                    return odt.toInstant();
                } catch (Exception ex2) {
                    try {
                        DateTimeFormatter fmt2 = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmssX");
                        OffsetDateTime odt2 = OffsetDateTime.parse(s, fmt2);
                        return odt2.toInstant();
                    } catch (Exception ex3) {
                        LocalDateTime ldt = LocalDateTime.parse(s);
                        return ldt.toInstant(ZoneOffset.UTC);
                    }
                }
            }
        }
    }

    private String formatInstantAsDbString(Instant inst) {
        if (inst == null) return null;
        ZonedDateTime zdt = inst.atZone(ZoneOffset.UTC);
        DateTimeFormatter out = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss.SSS'Z'");
        return zdt.format(out);
    }

    private String normalizeTagForDbComparisons(String t) {
        if (t == null) return null;
        String s = t.trim();
        if (s.startsWith("#")) s = s.substring(1);
        return s.toUpperCase();
    }

    private boolean tagEqualsNormalized(String a, String b) {
        if (a == null || b == null) return false;
        String na = normalizeTagForDbComparisons(a);
        String nb = normalizeTagForDbComparisons(b);
        return na != null && nb != null && na.equals(nb);
    }
}

