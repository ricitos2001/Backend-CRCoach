package org.example.backendcrcoach.web.controllers;

import jakarta.validation.Valid;
import org.example.backendcrcoach.domain.dto.PlayerProfileRequestDTO;
import org.example.backendcrcoach.domain.dto.PlayerProfileResponseDTO;
import org.example.backendcrcoach.services.PlayerProfileService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/v1/player_profiles", produces = MediaType.APPLICATION_JSON_VALUE)
public class PlayerProfileController {

    private final PlayerProfileService playerProfileService;

    public PlayerProfileController(PlayerProfileService playerProfileService) {
        this.playerProfileService = playerProfileService;
    }
    @Operation(summary = "Listar perfiles de jugador", description = "Obtiene una lista paginada de perfiles de jugador.")
    @GetMapping
    public ResponseEntity<Page<PlayerProfileResponseDTO>> list(Pageable pageable) {
        return ResponseEntity.ok(playerProfileService.list(pageable));
    }
    @Operation(summary = "Obtener perfil por ID", description = "Obtiene los detalles de un perfil de jugador por su ID.")
    @GetMapping("/id/{id}")
    public ResponseEntity<PlayerProfileResponseDTO> getById(@PathVariable(name = "id") Long id) {
        return ResponseEntity.ok(playerProfileService.showById(id));
    }
    @Operation(summary = "Obtener perfil por tag", description = "Obtiene el perfil de jugador asociado a un tag.")
    @GetMapping("/tag/{playerTag}")
    public ResponseEntity<PlayerProfileResponseDTO> getByTag(@PathVariable(name = "playerTag") String playerTag) {
        return ResponseEntity.ok(playerProfileService.showByTag(playerTag));
    }
    @Operation(summary = "Crear perfil de jugador", description = "Crea un nuevo perfil de jugador con los datos proporcionados.")
    @PostMapping
    public ResponseEntity<PlayerProfileResponseDTO> create(@RequestBody @Valid PlayerProfileRequestDTO dto) {
        PlayerProfileResponseDTO created = playerProfileService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    @Operation(summary = "Actualizar perfil de jugador", description = "Actualiza un perfil de jugador existente por su ID.")
    @PutMapping("/{id}")
    public ResponseEntity<PlayerProfileResponseDTO> update(@PathVariable(name = "id") Long id, @RequestBody @Valid PlayerProfileRequestDTO dto) {
        return ResponseEntity.ok(playerProfileService.update(id, dto));
    }
    @Operation(summary = "Eliminar perfil de jugador", description = "Elimina un perfil de jugador por su ID.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable(name = "id") Long id) {
        playerProfileService.delete(id);
        return ResponseEntity.noContent().build();
    }
    @Operation(summary = "Obtener perfil de jugador por tag (API externa)", description = "Recupera y devuelve información de jugador consultando la API externa por tag.")
    @GetMapping("/player/{tag}")
    public ResponseEntity<PlayerProfileResponseDTO> getPlayer(@PathVariable String tag) {
        PlayerProfileResponseDTO playerProfile = playerProfileService.getPlayer(tag);
        return ResponseEntity.ok(playerProfile);
    }

    @Operation(summary = "Comprobar existencia de tag", description = "Comprueba si un tag existe localmente o en la API de Clash Royale.")
    @GetMapping("/exists-in-local/{tag}")
    public ResponseEntity<Boolean> existsTagOnLocal(@PathVariable String tag) {
        boolean exists = playerProfileService.existsLocal(tag);
        return ResponseEntity.ok(exists);
    }

    @Operation(summary = "Comprobar existencia de tag", description = "Comprueba si un tag existe localmente o en la API de Clash Royale.")
    @GetMapping("/exists-in-api/{tag}")
    public ResponseEntity<Boolean> existsTagOnApi(@PathVariable String tag) {
        boolean exists = playerProfileService.exitsInApi(tag);
        return ResponseEntity.ok(exists);
    }
}

