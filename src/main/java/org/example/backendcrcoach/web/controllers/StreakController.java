package org.example.backendcrcoach.web.controllers;

import org.example.backendcrcoach.domain.dto.StreakRequestDTO;
import org.example.backendcrcoach.domain.dto.StreakResponseDTO;
import org.example.backendcrcoach.services.StreakService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/streaks")
public class StreakController {
    private final StreakService service;

    public StreakController(StreakService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<StreakResponseDTO> create(@RequestBody StreakRequestDTO dto) {
        StreakResponseDTO saved = service.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
}

