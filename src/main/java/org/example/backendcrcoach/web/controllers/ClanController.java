package org.example.backendcrcoach.web.controllers;

import org.example.backendcrcoach.domain.dto.ClanRequestDTO;
import org.example.backendcrcoach.domain.dto.ClanResponseDTO;
import org.example.backendcrcoach.services.ClanService;
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

    @PostMapping
    public ResponseEntity<ClanResponseDTO> create(@RequestBody ClanRequestDTO dto) {
        ClanResponseDTO created = clanService.createClan(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<ClanResponseDTO>> list() {
        return ResponseEntity.ok(clanService.getAllClans());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClanResponseDTO> getById(@PathVariable Long id) {
        return clanService.getClanById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClanResponseDTO> update(@PathVariable Long id, @RequestBody ClanRequestDTO dto) {
        return clanService.updateClan(id, dto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        clanService.deleteClan(id);
        return ResponseEntity.noContent().build();
    }
}

