package org.example.backendcrcoach.web.controllers;

import jakarta.validation.Valid;
import org.example.backendcrcoach.domain.dto.PlayerEntityRequestDTO;
import org.example.backendcrcoach.domain.dto.PlayerEntityResponseDTO;
import org.example.backendcrcoach.services.PlayerEntityService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/v1/player_entities", produces = MediaType.APPLICATION_JSON_VALUE)
public class PlayerEntityController {

    private final PlayerEntityService playerEntityService;

    public PlayerEntityController(PlayerEntityService playerEntityService) {
        this.playerEntityService = playerEntityService;
    }
    @Operation(summary = "Listar entidades de jugador", description = "Obtiene una lista paginada de entidades de jugador.")
    @GetMapping
    public ResponseEntity<Page<PlayerEntityResponseDTO>> list(Pageable pageable) {
        return ResponseEntity.ok(playerEntityService.list(pageable));
    }

    @Operation(summary = "Obtener entidad de jugador por ID", description = "Recupera una entidad de jugador por su ID.")
    @GetMapping("/id/{id}")
    public ResponseEntity<PlayerEntityResponseDTO> getById(@PathVariable(name = "id") Long id) {
        return ResponseEntity.ok(playerEntityService.showById(id));
    }

    @Operation(summary = "Obtener entidad de jugador por tag", description = "Recupera una entidad de jugador por su tag.")
    @GetMapping("/tag/{tag}")
    public ResponseEntity<PlayerEntityResponseDTO> getByTag(@PathVariable(name = "tag") String tag) {
        return ResponseEntity.ok(playerEntityService.showByTag(tag));
    }

    @Operation(summary = "Crear entidad de jugador", description = "Crea una nueva entidad de jugador.")
    @PostMapping
    public ResponseEntity<PlayerEntityResponseDTO> create(@RequestBody @Valid PlayerEntityRequestDTO dto) {
        PlayerEntityResponseDTO created = playerEntityService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Actualizar entidad de jugador", description = "Actualiza una entidad de jugador por su ID.")
    @PutMapping("/{id}")
    public ResponseEntity<PlayerEntityResponseDTO> update(@PathVariable(name = "id") Long id,
                                                          @RequestBody @Valid PlayerEntityRequestDTO dto) {
        return ResponseEntity.ok(playerEntityService.update(id, dto));
    }

    @Operation(summary = "Eliminar entidad de jugador", description = "Elimina una entidad de jugador por su ID.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable(name = "id") Long id) {
        playerEntityService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

