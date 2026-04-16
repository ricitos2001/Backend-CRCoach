package org.example.backendcrcoach.web.controllers;

import jakarta.validation.Valid;
import org.example.backendcrcoach.domain.dto.GoalRequestDTO;
import org.example.backendcrcoach.domain.dto.GoalResponseDTO;
import org.example.backendcrcoach.services.GoalService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;
import io.swagger.v3.oas.annotations.Operation;
import org.example.backendcrcoach.domain.enums.GoalStatus;
import java.util.List;


@RestController
@RequestMapping(value = "/api/v1/goals", produces = MediaType.APPLICATION_JSON_VALUE)
public class GoalController {

    private final GoalService goalService;

    public GoalController(GoalService goalService) {
        this.goalService = goalService;
    }
    @Operation(summary = "Listar objetivos", description = "Obtiene una lista paginada de objetivos (goals).")
    @GetMapping
    public ResponseEntity<Page<GoalResponseDTO>> list(Pageable pageable) {
        Page<GoalResponseDTO> goals = goalService.list(pageable);
        return ResponseEntity.ok(goals);
    }
    @Operation(summary = "Obtener objetivo por ID", description = "Recupera un objetivo por su ID.")
    @GetMapping("/id/{id}")
    public ResponseEntity<GoalResponseDTO> getById(@PathVariable(name = "id") Long id) {
        GoalResponseDTO goal = goalService.showById(id);
        return ResponseEntity.ok(goal);
    }
    @Operation(summary = "Obtener objetivo por título", description = "Obtiene un objetivo por su título.")
    @GetMapping("/title/{title}")
    public ResponseEntity<GoalResponseDTO> getByName(@PathVariable(name = "title") String title) {
        GoalResponseDTO goal = goalService.showByTitle(title);
        return ResponseEntity.ok(goal);
    }
    @Operation(summary = "Crear objetivo", description = "Crea un nuevo objetivo con los datos proporcionados.")
    @PostMapping
    public ResponseEntity<GoalResponseDTO> create(@RequestBody @Valid GoalRequestDTO dto) {
        GoalResponseDTO saved = goalService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
    @Operation(summary = "Actualizar objetivo", description = "Actualiza un objetivo existente por su ID.")
    @PutMapping("/{id}")
    public ResponseEntity<GoalResponseDTO> update(@PathVariable(name = "id") Long id, @RequestBody @Valid GoalRequestDTO dto) {
        GoalResponseDTO toggled = goalService.update(id, dto);
        return ResponseEntity.ok(toggled);
    }
    @Operation(summary = "Eliminar objetivo", description = "Elimina un objetivo por su ID.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable(name = "id") Long id) {
        goalService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Listar objetivos por usuario", description = "Obtiene la lista de objetivos asociados al email del usuario. Se puede filtrar por estado opcionalmente (IN_PROGRESS, COMPLETED, FAILED).")
    @GetMapping("/user")
    public ResponseEntity<List<GoalResponseDTO>> listByUserEmail(@RequestParam(name = "email") String email,
                                                                 @RequestParam(name = "status", required = false) String status) {
        List<GoalResponseDTO> goals;
        if (status == null || status.isBlank()) {
            goals = goalService.listByUserEmail(email);
        } else {
            GoalStatus gs;
            try {
                gs = GoalStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().build();
            }
            goals = goalService.listByUserEmailAndStatus(email, gs);
        }
        return ResponseEntity.ok(goals);
    }
}
