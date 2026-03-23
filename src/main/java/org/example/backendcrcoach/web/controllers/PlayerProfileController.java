package org.example.backendcrcoach.web.controllers;

import jakarta.validation.Valid;
import org.example.backendcrcoach.domain.dto.PlayerProfileRequestDTO;
import org.example.backendcrcoach.domain.dto.PlayerProfileResponseDTO;
import org.example.backendcrcoach.services.PlayerProfileService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/v1/player_profiles", produces = MediaType.APPLICATION_JSON_VALUE)
public class PlayerProfileController {

    private final PlayerProfileService playerProfileService;

    public PlayerProfileController(PlayerProfileService playerProfileService) {
        this.playerProfileService = playerProfileService;
    }

    @GetMapping
    public ResponseEntity<Page<PlayerProfileResponseDTO>> list(Pageable pageable) {
        return ResponseEntity.ok(playerProfileService.list(pageable));
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<PlayerProfileResponseDTO> getById(@PathVariable(name = "id") Long id) {
        return ResponseEntity.ok(playerProfileService.showById(id));
    }

    @GetMapping("/tag/{playerTag}")
    public ResponseEntity<PlayerProfileResponseDTO> getByTag(@PathVariable(name = "playerTag") String playerTag) {
        return ResponseEntity.ok(playerProfileService.showByTag(playerTag));
    }

    @PostMapping
    public ResponseEntity<PlayerProfileResponseDTO> create(@RequestBody @Valid PlayerProfileRequestDTO dto) {
        PlayerProfileResponseDTO created = playerProfileService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PlayerProfileResponseDTO> update(@PathVariable(name = "id") Long id, @RequestBody @Valid PlayerProfileRequestDTO dto) {
        return ResponseEntity.ok(playerProfileService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable(name = "id") Long id) {
        playerProfileService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/player/{tag}")
    public ResponseEntity<PlayerProfileResponseDTO> getPlayer(@PathVariable String tag) {
        PlayerProfileResponseDTO playerProfile = playerProfileService.getPlayer(tag);
        return ResponseEntity.ok(playerProfile);
    }
}

