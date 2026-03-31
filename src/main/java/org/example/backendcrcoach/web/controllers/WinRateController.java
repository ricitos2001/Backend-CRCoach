package org.example.backendcrcoach.web.controllers;

import org.example.backendcrcoach.domain.dto.WinRateRequestDTO;
import org.example.backendcrcoach.domain.dto.WinRateResponseDTO;
import org.example.backendcrcoach.services.WinRateService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/winrates")
public class WinRateController {
    private final WinRateService service;

    public WinRateController(WinRateService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<WinRateResponseDTO> create(@RequestBody WinRateRequestDTO dto) {
        WinRateResponseDTO saved = service.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
}

