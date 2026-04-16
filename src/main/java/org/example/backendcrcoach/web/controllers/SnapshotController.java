package org.example.backendcrcoach.web.controllers;
import io.swagger.v3.oas.annotations.Operation;
import org.example.backendcrcoach.domain.dto.SnapshotResponseDTO;
import org.example.backendcrcoach.domain.entities.Snapshot;
import org.example.backendcrcoach.mappers.SnapshotMapper;
import org.example.backendcrcoach.services.SnapshotService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/snapshots", produces = MediaType.APPLICATION_JSON_VALUE)
public class SnapshotController {
    private final SnapshotService snapshotService;
    public SnapshotController(SnapshotService snapshotService) {
        this.snapshotService = snapshotService;
    }
    @GetMapping("/player-tag/{playerTag}")
    @Operation(summary = "Obtener snapshots por tag de jugador", description = "Obtiene todas las instantáneas capturadas de un jugador ordenadas por fecha descendente.")
    public ResponseEntity<Page<SnapshotResponseDTO>> getSnapshotsByPlayerTag(@PathVariable String playerTag, Pageable pageable) {
        Page<Snapshot> snapshots = snapshotService.getSnapshotsByPlayerTag(playerTag, pageable);
        Page<SnapshotResponseDTO> dtoPage = snapshots.map(SnapshotMapper::toDTO);
        return ResponseEntity.ok(dtoPage);
    }

    @GetMapping("/player-profile/{playerProfileId}")
    @Operation(summary = "Obtener snapshots por ID de perfil de jugador", description = "Obtiene todas las instantáneas capturadas de un perfil de jugador por su ID.")
    public ResponseEntity<Page<SnapshotResponseDTO>> getSnapshotsByPlayerProfileId(@PathVariable Long playerProfileId, Pageable pageable) {
        Page<Snapshot> snapshots = snapshotService.getSnapshotsByPlayerProfileId(playerProfileId, pageable);
        Page<SnapshotResponseDTO> dtoPage = snapshots.map(SnapshotMapper::toDTO);
        return ResponseEntity.ok(dtoPage);
    }
    @GetMapping("/{id}")
    @Operation(summary = "Obtener snapshot por ID", description = "Obtiene una instantánea específica por su ID.")
    public ResponseEntity<SnapshotResponseDTO> getSnapshotById(@PathVariable Long id) {
        Snapshot snapshot = snapshotService.getSnapshotById(id);
        return ResponseEntity.ok(SnapshotMapper.toDTO(snapshot));
    }
    @GetMapping("/player-tag/{playerTag}/range")
    @Operation(summary = "Obtener snapshots en rango de fechas", 
            description = "Obtiene las instantáneas capturadas dentro de un rango de fechas para un jugador específico.")
    public ResponseEntity<Page<SnapshotResponseDTO>> getSnapshotsByDateRange(@PathVariable String playerTag, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to, Pageable pageable) {
        Page<Snapshot> snapshots = snapshotService.getSnapshotsByDateRange(playerTag, from, to, pageable);
        Page<SnapshotResponseDTO> dtoPage = snapshots.map(SnapshotMapper::toDTO);
        return ResponseEntity.ok(dtoPage);
    }

    @GetMapping("/{playerTag}/latest")
    @Operation(summary = "Obtener último snapshot por tag de jugador", description = "Obtiene la última instantánea tomada para un jugador dado su tag.")
    public ResponseEntity<SnapshotResponseDTO> getLatestSnapshotByPlayerTag(@PathVariable String playerTag) {
        return snapshotService.getLatestSnapshotByPlayerTag(playerTag)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/mySnapshots/{playerTag}")
    @Operation(summary = "Obtener historial de snapshots por tag de jugador", description = "Obtiene el registro completo de snapshots (orden descendente) para un jugador.")
    public ResponseEntity<List<SnapshotResponseDTO>> getSnapshotHistoryByPlayerTag(@PathVariable String playerTag) {
        List<SnapshotResponseDTO> history = snapshotService.getSnapshotHistoryByPlayerTag(playerTag);
        return ResponseEntity.ok(history);
    }
    @DeleteMapping("/cleanup")
    @Operation(summary = "Eliminar snapshots antiguas", description = "Elimina todas las instantáneas más antiguas que X días.")
    public ResponseEntity<String> deleteOldSnapshots(@RequestParam(defaultValue = "30") int daysOld) {
        snapshotService.deleteOldSnapshots(daysOld);
        return ResponseEntity.ok("Snapshots antiguas eliminadas (más de " + daysOld + " días).");
    }
}
