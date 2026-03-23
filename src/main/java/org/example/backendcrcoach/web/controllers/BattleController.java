package org.example.backendcrcoach.web.controllers;

import org.example.backendcrcoach.domain.dto.BattleRequestDTO;
import org.example.backendcrcoach.domain.dto.BattleResponseDTO;
import org.example.backendcrcoach.services.BattleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/battles")
public class BattleController {

    private final BattleService battleService;

    public BattleController(BattleService battleService) {
        this.battleService = battleService;
    }

    @PostMapping
    public ResponseEntity<BattleResponseDTO> create(@RequestBody BattleRequestDTO dto) {
        BattleResponseDTO created = battleService.createBattle(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<BattleResponseDTO>> list() {
        return ResponseEntity.ok(battleService.getAllBattles());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BattleResponseDTO> getById(@PathVariable Long id) {
        return battleService.getBattleById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<BattleResponseDTO> update(@PathVariable Long id, @RequestBody BattleRequestDTO dto) {
        return battleService.updateBattle(id, dto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        battleService.deleteBattle(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/import/{playerTag}")
    public ResponseEntity<String> importForPlayer(@PathVariable String playerTag) {
        int imported = battleService.importBattlesForPlayer(playerTag);
        return ResponseEntity.ok("Imported " + imported + " battles for player " + playerTag);
    }
}

