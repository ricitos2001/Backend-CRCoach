package org.example.backendcrcoach.web.controllers;

import io.swagger.v3.oas.annotations.Operation;
import org.example.backendcrcoach.domain.dto.MetricResponseDTO;
import org.example.backendcrcoach.services.MetricsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/metrics")
public class MetricsController {

    private final MetricsService metricsService;

    public MetricsController(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @GetMapping("/player/{tag}/summary")
    @Operation(summary = "Resumen de métricas del jugador para dashboard")
    public ResponseEntity<MetricResponseDTO> getPlayerSummary(@PathVariable("tag") String tag,
                                                               @RequestParam(value = "battles", required = false) Integer battles) {
        // Normalizar el tag: aceptar tanto con '#' como sin él
        String normalizedTag = tag;
        if (normalizedTag != null && !normalizedTag.startsWith("#")) {
            normalizedTag = "#" + normalizedTag;
        }

        MetricResponseDTO dto = metricsService.getPlayerSummary(normalizedTag, battles);
        if (dto == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(dto);
    }
}

