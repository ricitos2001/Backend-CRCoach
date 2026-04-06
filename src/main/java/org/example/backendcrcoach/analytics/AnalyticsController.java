package org.example.backendcrcoach.analytics;

import org.example.backendcrcoach.analytics.dto.PlayerSummaryDto;
import org.example.backendcrcoach.analytics.dto.ProblematicCardDto;
import org.example.backendcrcoach.analytics.dto.ProblematicCardsReportDto;
import org.example.backendcrcoach.analytics.dto.WeaknessReportDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/player/{tag}/weaknesses")
    public ResponseEntity<WeaknessReportDto> getWeaknesses(
            @PathVariable("tag") String tag,
            @RequestParam(required = false) String gameMode,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) Integer minBattles
    ) {
        String normalizedTag = tag;
        if (normalizedTag != null && !normalizedTag.startsWith("#")) {
            normalizedTag = "#" + normalizedTag;
        }
        WeaknessReportDto report = analyticsService.getWeaknesses(normalizedTag, gameMode, from, to, minBattles);
        // Persistir informe en base de datos
        analyticsService.saveWeaknessReport(report);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/player/{tag}/cards") public ResponseEntity<ProblematicCardsReportDto> getProblematicCards(
            @PathVariable("tag") String tag,
            @RequestParam(required = false) String gameMode,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) Integer minAppearances
    ) {
        String normalizedTag = tag;
        if (normalizedTag != null && !normalizedTag.startsWith("#")) {
            normalizedTag = "#" + normalizedTag;
        }
        List<ProblematicCardDto> cards = analyticsService.getProblematicCards(normalizedTag, gameMode, from, to, limit, minAppearances);
        org.example.backendcrcoach.analytics.dto.ProblematicCardsReportDto report = new org.example.backendcrcoach.analytics.dto.ProblematicCardsReportDto();
        report.setPlayerTag(normalizedTag);
        // compute total losses consistently with service
        long totalLosses = analyticsService.getTotalLossesForFilter(normalizedTag, gameMode, from, to);
        report.setTotalLosses(totalLosses);
        report.setProblematicCards(cards);
        // Persistir informe en base de datos
        analyticsService.saveProblematicCardsReport(report);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/player/{tag}/summary")
    public ResponseEntity<PlayerSummaryDto> getPlayerSummary(
            @PathVariable("tag") String tag,
            @RequestParam(required = false) String gameMode,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to
    ) {
        String normalizedTag = tag;
        if (normalizedTag != null && !normalizedTag.startsWith("#")) {
            normalizedTag = "#" + normalizedTag;
        }
        PlayerSummaryDto summary = analyticsService.getPlayerSummary(normalizedTag, gameMode, from, to);
        // Persistir resumen en base de datos
        analyticsService.savePlayerSummary(summary);
        return ResponseEntity.ok(summary);
    }
}

