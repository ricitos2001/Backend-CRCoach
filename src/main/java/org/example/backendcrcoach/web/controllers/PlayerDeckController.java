package org.example.backendcrcoach.web.controllers;

import org.example.backendcrcoach.domain.dto.PlayerCardResponseDTO;
import org.example.backendcrcoach.domain.dto.PlayerDeckRequestDTO;
import org.example.backendcrcoach.domain.dto.PlayerDeckResponseDTO;
import org.example.backendcrcoach.services.PlayerDeckService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/player-decks")
public class PlayerDeckController {

    private final PlayerDeckService playerDeckService;

    public PlayerDeckController(PlayerDeckService playerDeckService) {
        this.playerDeckService = playerDeckService;
    }

    @GetMapping
    public ResponseEntity<List<PlayerDeckResponseDTO>> list() {
        return ResponseEntity.ok(playerDeckService.listAll());
    }

    @PostMapping
    public ResponseEntity<PlayerDeckResponseDTO> create(@RequestBody PlayerDeckRequestDTO dto) {
        PlayerDeckResponseDTO created = playerDeckService.create(dto);
        return ResponseEntity.created(URI.create("/api/player-decks/" + created.getId())).body(created);
    }

    @PostMapping("/import-cards/{playerTag}")
    public ResponseEntity<List<PlayerCardResponseDTO>> importCardsFromBattles(@PathVariable String playerTag) {
        List<PlayerCardResponseDTO> imported = playerDeckService.importCardsFromBattles(playerTag);
        return ResponseEntity.ok(imported);
    }
}

