package org.example.backendcrcoach.web.controllers;

import org.example.backendcrcoach.domain.dto.DeckRequestDTO;
import org.example.backendcrcoach.domain.dto.DeckResponseDTO;
import org.example.backendcrcoach.services.DeckService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/decks")
public class DeckController {

    private final DeckService deckService;

    public DeckController(DeckService deckService) {
        this.deckService = deckService;
    }

    @GetMapping
    public ResponseEntity<List<DeckResponseDTO>> list() {
        return ResponseEntity.ok(deckService.listAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeckResponseDTO> getById(@PathVariable Long id) {
        return deckService.findById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/api/{apiId}")
    public ResponseEntity<DeckResponseDTO> getByApiId(@PathVariable Long apiId) {
        return deckService.findByApiId(apiId).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<DeckResponseDTO> create(@RequestBody DeckRequestDTO dto) {
        DeckResponseDTO created = deckService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DeckResponseDTO> update(@PathVariable Long id, @RequestBody DeckRequestDTO dto) {
        return deckService.update(id, dto).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deckService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

