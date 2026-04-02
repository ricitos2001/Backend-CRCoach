package org.example.backendcrcoach.web.controllers;

import org.example.backendcrcoach.domain.dto.BattleRequestDTO;
import org.example.backendcrcoach.domain.dto.BattleResponseDTO;
import org.example.backendcrcoach.services.BattleService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/battles")
public class BattleController {

    private final BattleService battleService;

    public BattleController(BattleService battleService) {
        this.battleService = battleService;
    }

    @Operation(summary = "Crear batalla", description = "Crea una nueva batalla con los datos proporcionados.")
    @PostMapping
    public ResponseEntity<BattleResponseDTO> create(@RequestBody BattleRequestDTO dto) {
        BattleResponseDTO created = battleService.createBattle(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Listar batallas", description = "Obtiene la lista de todas las batallas almacenadas.")
    @GetMapping
    public ResponseEntity<List<BattleResponseDTO>> list() {
        return ResponseEntity.ok(battleService.getAllBattles());
    }

    @Operation(summary = "Obtener batalla por ID", description = "Recupera los detalles de una batalla dada su ID.")
    @GetMapping("/{id}")
    public ResponseEntity<BattleResponseDTO> getById(@PathVariable Long id) {
        return battleService.getBattleById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Actualizar batalla", description = "Actualiza una batalla existente por su ID con los nuevos datos.")
    @PutMapping("/{id}")
    public ResponseEntity<BattleResponseDTO> update(@PathVariable Long id, @RequestBody BattleRequestDTO dto) {
        return battleService.updateBattle(id, dto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Eliminar batalla", description = "Elimina una batalla existente por su ID.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        battleService.deleteBattle(id);
        return ResponseEntity.noContent().build();
    }
    @Operation(summary = "Importar batallas de jugador", description = "Importa batallas desde la API externa para un jugador dado su tag.")
    @PostMapping("/import/{playerTag}")
    public ResponseEntity<String> importForPlayer(@PathVariable String playerTag) {
        // Lanzar la importación en segundo plano y devolver 202 Accepted inmediatamente.
        battleService.importBattlesForPlayerAsync(playerTag);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body("Import started for player " + playerTag);
    }
}

