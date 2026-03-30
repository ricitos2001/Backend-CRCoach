package org.example.backendcrcoach.web.controllers;

import org.example.backendcrcoach.domain.dto.SeasonRequestDTO;
import org.example.backendcrcoach.domain.dto.SeasonResponseDTO;
import org.example.backendcrcoach.services.SeasonService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/seasons")
public class SeasonController {

    private final SeasonService seasonService;

    public SeasonController(SeasonService seasonService) {
        this.seasonService = seasonService;
    }

    @Operation(summary = "Crear season", description = "Crea una nueva season con los datos proporcionados.")
    @PostMapping
    public ResponseEntity<SeasonResponseDTO> create(@RequestBody SeasonRequestDTO dto) {
        SeasonResponseDTO created = seasonService.createSeason(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Listar seasons", description = "Obtiene la lista de todas las seasons.")
    @GetMapping
    public ResponseEntity<List<SeasonResponseDTO>> list() {
        return ResponseEntity.ok(seasonService.getAllSeasons());
    }

    @Operation(summary = "Obtener season por ID", description = "Recupera los detalles de una season por su ID.")
    @GetMapping("/{id}")
    public ResponseEntity<SeasonResponseDTO> getById(@PathVariable Long id) {
        return seasonService.getSeasonById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Actualizar season", description = "Actualiza una season existente por su ID.")
    @PutMapping("/{id}")
    public ResponseEntity<SeasonResponseDTO> update(@PathVariable Long id, @RequestBody SeasonRequestDTO dto) {
        return seasonService.updateSeason(id, dto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Eliminar season", description = "Elimina una season por su ID.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        seasonService.deleteSeason(id);
        return ResponseEntity.noContent().build();
    }
}

