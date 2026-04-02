package org.example.backendcrcoach.web.controllers;

import org.example.backendcrcoach.domain.dto.GameModeRequestDTO;
import org.example.backendcrcoach.domain.dto.GameModeResponseDTO;
import org.example.backendcrcoach.services.GameModeService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/game-modes")
public class GameModeController {

    private final GameModeService gameModeService;

    public GameModeController(GameModeService gameModeService) {
        this.gameModeService = gameModeService;
    }

    @Operation(summary = "Crear game mode", description = "Crea un nuevo modo de juego.")
    @PostMapping
    public ResponseEntity<GameModeResponseDTO> create(@RequestBody GameModeRequestDTO dto) {
        GameModeResponseDTO created = gameModeService.createGameMode(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Listar game modes", description = "Obtiene la lista de todos los modos de juego.")
    @GetMapping
    public ResponseEntity<List<GameModeResponseDTO>> list() {
        return ResponseEntity.ok(gameModeService.getAllGameModes());
    }

    @Operation(summary = "Obtener game mode por ID", description = "Recupera los detalles de un modo de juego por su ID.")
    @GetMapping("/{id}")
    public ResponseEntity<GameModeResponseDTO> getById(@PathVariable Long id) {
        return gameModeService.getGameModeById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Actualizar game mode", description = "Actualiza un modo de juego existente por su ID.")
    @PutMapping("/{id}")
    public ResponseEntity<GameModeResponseDTO> update(@PathVariable Long id, @RequestBody GameModeRequestDTO dto) {
        return gameModeService.updateGameMode(id, dto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Eliminar game mode", description = "Elimina un modo de juego por su ID.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        gameModeService.deleteGameMode(id);
        return ResponseEntity.noContent().build();
    }
}

