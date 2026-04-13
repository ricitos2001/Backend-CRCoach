package org.example.backendcrcoach.analytics;

import io.swagger.v3.oas.annotations.Operation;
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

    @Operation(summary = "Obtiene un informe de las debilidades de un jugador específico, basado en sus derrotas. Se pueden aplicar filtros por modo de juego, rango de fechas y número mínimo de batallas.")
    @GetMapping("/player/{tag}/weaknesses")
    public ResponseEntity<WeaknessReportDto> getWeaknesses(
            @PathVariable("tag") String tag,
            @RequestParam(required = false) String gameMode,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) Integer minBattles
    ) {
        WeaknessReportDto report = analyticsService.getWeaknesses(tag, gameMode, from, to, minBattles);
        // Persistir informe en base de datos
        analyticsService.saveWeaknessReport(report);
        return ResponseEntity.ok(report);
    }

    @Operation(summary = "Obtiene un informe de las cartas más problemáticas para un jugador específico, basado en sus derrotas. Se pueden aplicar filtros por modo de juego, rango de fechas, número mínimo de apariciones y límite de resultados.")
    @GetMapping("/player/{tag}/cards") public ResponseEntity<ProblematicCardsReportDto> getProblematicCards(
            @PathVariable("tag") String tag,
            @RequestParam(required = false) String gameMode,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) Integer minAppearances
    ) {
        List<ProblematicCardDto> cards = analyticsService.getProblematicCards(tag, gameMode, from, to, limit, minAppearances);
        ProblematicCardsReportDto report = new ProblematicCardsReportDto();
        report.setPlayerTag(tag);
        long totalLosses = analyticsService.getTotalLossesForFilter(tag, gameMode, from, to);
        report.setTotalLosses(totalLosses);
        report.setProblematicCards(cards);
        analyticsService.saveProblematicCardsReport(report);
        return ResponseEntity.ok(report);
    }

    @Operation(summary = "Obtiene un resumen de las estadísticas de un jugador específico, incluyendo su rendimiento general, tendencias y áreas de mejora. Se pueden aplicar filtros por modo de juego y rango de fechas.")
    @GetMapping("/player/{tag}/summary")
    public ResponseEntity<PlayerSummaryDto> getPlayerSummary(
            @PathVariable("tag") String tag,
            @RequestParam(required = false) String gameMode,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to
    ) {
        PlayerSummaryDto summary = analyticsService.getPlayerSummary(tag, gameMode, from, to);
        // Persistir resumen en base de datos
        analyticsService.savePlayerSummary(summary);
        return ResponseEntity.ok(summary);
    }
}

