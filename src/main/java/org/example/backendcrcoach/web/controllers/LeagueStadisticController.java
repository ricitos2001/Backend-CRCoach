package org.example.backendcrcoach.web.controllers;

import org.example.backendcrcoach.domain.dto.LeagueStadisticRequestDTO;
import org.example.backendcrcoach.domain.dto.LeagueStadisticResponseDTO;
import org.example.backendcrcoach.services.LeagueStadisticService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/league_stadistics")
public class LeagueStadisticController {

    private final LeagueStadisticService service;

    public LeagueStadisticController(LeagueStadisticService service) {
        this.service = service;
    }

    @Operation(summary = "Crear league stadistic")
    @PostMapping
    public ResponseEntity<LeagueStadisticResponseDTO> create(@RequestBody LeagueStadisticRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @Operation(summary = "Listar league stadistics")
    @GetMapping
    public ResponseEntity<List<LeagueStadisticResponseDTO>> list() {
        return ResponseEntity.ok(service.list());
    }

    @Operation(summary = "Obtener por id")
    @GetMapping("/{id}")
    public ResponseEntity<LeagueStadisticResponseDTO> getById(@PathVariable Long id) {
        return service.getById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Actualizar league stadistic")
    @PutMapping("/{id}")
    public ResponseEntity<LeagueStadisticResponseDTO> update(@PathVariable Long id, @RequestBody LeagueStadisticRequestDTO dto) {
        return service.update(id, dto).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Eliminar league stadistic")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

