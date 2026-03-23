package org.example.backendcrcoach.web.controllers;

import org.example.backendcrcoach.domain.dto.PlayerCardRequestDTO;
import org.example.backendcrcoach.domain.dto.PlayerCardResponseDTO;
import org.example.backendcrcoach.services.PlayerCardService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/player_cards")
public class PlayerCardController {

    private final PlayerCardService playerCardService;

    public PlayerCardController(PlayerCardService playerCardService) {
        this.playerCardService = playerCardService;
    }

    @GetMapping
    public ResponseEntity<List<PlayerCardResponseDTO>> list() {
        return ResponseEntity.ok(playerCardService.listAll());
    }

    @PostMapping
    public ResponseEntity<PlayerCardResponseDTO> create(@RequestBody PlayerCardRequestDTO dto) {
        PlayerCardResponseDTO created = playerCardService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/import/{playerTag}")
    public ResponseEntity<List<PlayerCardResponseDTO>> importForPlayer(@PathVariable String playerTag) {
        List<PlayerCardResponseDTO> imported = playerCardService.importCardsForPlayer(playerTag);
        return ResponseEntity.ok(imported);
    }
}

