package org.example.backendcrcoach.web.controllers;

import org.example.backendcrcoach.domain.dto.ActiveGoalsRequestDTO;
import org.example.backendcrcoach.domain.dto.ActiveGoalsResponseDTO;
import org.example.backendcrcoach.services.ActiveGoalsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/active-goals")
public class ActiveGoalsController {
    private final ActiveGoalsService service;

    public ActiveGoalsController(ActiveGoalsService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<ActiveGoalsResponseDTO> create(@RequestBody ActiveGoalsRequestDTO dto) {
        ActiveGoalsResponseDTO saved = service.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
}

