package org.example.backendcrcoach.web.controllers;

import org.example.backendcrcoach.domain.dto.BattlesRequestDTO;
import org.example.backendcrcoach.domain.dto.BattlesResponseDTO;
import org.example.backendcrcoach.services.BattlesService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/battles-stats")
public class BattlesController {
    private final BattlesService service;

    public BattlesController(BattlesService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<BattlesResponseDTO> create(@RequestBody BattlesRequestDTO dto) {
        BattlesResponseDTO saved = service.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
}

