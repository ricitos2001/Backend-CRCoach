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

    @Operation(summary = "Obtener batallas por tag de jugador", description = "Recupera las batallas donde aparece el jugador (como team u opponent).")
    @GetMapping("/myBattles/{playerTag}")
    public ResponseEntity<List<BattleResponseDTO>> getByPlayerTag(@PathVariable String playerTag,
                                                                 @RequestParam(required = false) Integer limit) {
        List<BattleResponseDTO> battles = battleService.getBattlesByPlayerTag(playerTag, limit);
        return ResponseEntity.ok(battles);
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
    @Operation(summary = "Importar batallas de jugador (síncrono)", description = "Importa batallas desde la API externa para un jugador dado su tag y devuelve el resultado.")
    @GetMapping("/import/{playerTag}")
    public ResponseEntity<BattleResponseDTO> importForPlayer(@PathVariable String playerTag) {
        // Ejecutar la importación de forma síncrona y devolver el DTO de la última batalla importada
        BattleResponseDTO result = battleService.importBattlesForPlayer(playerTag);
        if (result == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(result);
    }
}

