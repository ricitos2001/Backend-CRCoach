package org.example.backendcrcoach.web.controllers;

import org.example.backendcrcoach.domain.dto.ClanRequestDTO;
import org.example.backendcrcoach.domain.dto.ClanResponseDTO;
import org.example.backendcrcoach.services.ClanService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/clans")
public class ClanController {

    private final ClanService clanService;

    public ClanController(ClanService clanService) {
        this.clanService = clanService;
    }
    @Operation(summary = "Crear clan", description = "Crea un nuevo clan con los datos proporcionados.")
    @PostMapping
    public ResponseEntity<ClanResponseDTO> create(@RequestBody ClanRequestDTO dto) {
        ClanResponseDTO created = clanService.createClan(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Listar clanes", description = "Devuelve la lista de todos los clanes.")
    @GetMapping
    public ResponseEntity<List<ClanResponseDTO>> list() {
        return ResponseEntity.ok(clanService.getAllClans());
    }

    @Operation(summary = "Obtener clan por ID", description = "Recupera un clan por su ID.")
    @GetMapping("/{id}")
    public ResponseEntity<ClanResponseDTO> getById(@PathVariable Long id) {
        return clanService.getClanById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Actualizar clan", description = "Actualiza un clan existente por su ID.")
    @PutMapping("/{id}")
    public ResponseEntity<ClanResponseDTO> update(@PathVariable Long id, @RequestBody ClanRequestDTO dto) {
        return clanService.updateClan(id, dto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Eliminar clan", description = "Elimina un clan por su ID.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        clanService.deleteClan(id);
        return ResponseEntity.noContent().build();
    }
}

