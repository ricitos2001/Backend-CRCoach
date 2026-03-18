package org.example.backendcrcoach.web.controllers;

import org.example.backendcrcoach.domain.dto.CardRequestDTO;
import org.example.backendcrcoach.domain.dto.CardResponseDTO;
import org.example.backendcrcoach.services.CardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/cards")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @GetMapping
    public ResponseEntity<List<CardResponseDTO>> list() {
        return ResponseEntity.ok(cardService.listAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CardResponseDTO> getById(@PathVariable Long id) {
        return cardService.findById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<CardResponseDTO> create(@RequestBody CardRequestDTO dto) {
        CardResponseDTO created = cardService.create(dto);
        return ResponseEntity.created(URI.create("/api/cards/" + created.getId())).body(created);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        cardService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/import")
    public ResponseEntity<String> importAll() {
        int imported = cardService.importAllCardsFromApi();
        return ResponseEntity.ok("Imported " + imported + " cards");
    }
}

