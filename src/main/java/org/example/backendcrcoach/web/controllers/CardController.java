package org.example.backendcrcoach.web.controllers;

import org.example.backendcrcoach.domain.dto.CardRequestDTO;
import org.example.backendcrcoach.domain.dto.CardResponseDTO;
import org.example.backendcrcoach.services.CardService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cards")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }
    @Operation(summary = "Listar cartas", description = "Devuelve la lista de todas las cartas.")
    @GetMapping
    public ResponseEntity<List<CardResponseDTO>> list() {
        return ResponseEntity.ok(cardService.listAll());
    }

    @Operation(summary = "Obtener carta por ID", description = "Recupera los detalles de una carta por su ID.")
    @GetMapping("/{id}")
    public ResponseEntity<CardResponseDTO> getById(@PathVariable Long id) {
        return cardService.findById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Crear carta", description = "Crea una nueva carta con los datos proporcionados.")
    @PostMapping
    public ResponseEntity<CardResponseDTO> create(@RequestBody CardRequestDTO dto) {
        CardResponseDTO created = cardService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Eliminar carta", description = "Elimina una carta por su ID.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        cardService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Importar todas las cartas", description = "Importa todas las cartas desde la API externa.")
    @GetMapping("/import")
    public ResponseEntity<String> importAll() {
        int imported = cardService.importAllCardsFromApi();
        return ResponseEntity.ok("Imported " + imported + " cards");
    }
}

