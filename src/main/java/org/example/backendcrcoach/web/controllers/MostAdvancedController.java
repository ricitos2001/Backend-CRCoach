package org.example.backendcrcoach.web.controllers;

import org.example.backendcrcoach.domain.dto.MostAdvancedRequestDTO;
import org.example.backendcrcoach.domain.dto.MostAdvancedResponseDTO;
import org.example.backendcrcoach.services.MostAdvancedService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/most_advanced")
public class MostAdvancedController {
    private final MostAdvancedService service;

    public MostAdvancedController(MostAdvancedService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<MostAdvancedResponseDTO> create(@RequestBody MostAdvancedRequestDTO dto) {
        MostAdvancedResponseDTO saved = service.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
}

