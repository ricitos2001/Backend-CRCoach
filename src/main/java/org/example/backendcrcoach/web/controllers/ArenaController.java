package org.example.backendcrcoach.web.controllers;

import org.example.backendcrcoach.domain.dto.ArenaRequestDTO;
import org.example.backendcrcoach.domain.dto.ArenaResponseDTO;
import org.example.backendcrcoach.services.ArenaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/arenas")
public class ArenaController {

    private final ArenaService arenaService;

    public ArenaController(ArenaService arenaService) {
        this.arenaService = arenaService;
    }

    @PostMapping
    public ResponseEntity<ArenaResponseDTO> create(@RequestBody ArenaRequestDTO dto) {
        ArenaResponseDTO created = arenaService.createArena(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<ArenaResponseDTO>> list() {
        return ResponseEntity.ok(arenaService.getAllArenas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ArenaResponseDTO> getById(@PathVariable Long id) {
        return arenaService.getArenaById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ArenaResponseDTO> update(@PathVariable Long id, @RequestBody ArenaRequestDTO dto) {
        return arenaService.updateArena(id, dto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        arenaService.deleteArena(id);
        return ResponseEntity.noContent().build();
    }
}

