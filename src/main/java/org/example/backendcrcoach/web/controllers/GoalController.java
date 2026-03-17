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


@RestController
@RequestMapping(value = "/api/v1/goals", produces = MediaType.APPLICATION_JSON_VALUE)
public class GoalController {

    private final GoalService goalService;

    public GoalController(GoalService goalService) {
        this.goalService = goalService;
    }

    @GetMapping
    public ResponseEntity<Page<GoalResponseDTO>> list(Pageable pageable) {
        Page<GoalResponseDTO> goals = goalService.list(pageable);
        return ResponseEntity.ok(goals);
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<GoalResponseDTO> getById(@PathVariable(name = "id") Long id) {
        GoalResponseDTO goal = goalService.showById(id);
        return ResponseEntity.ok(goal);
    }

    @GetMapping("/title/{title}")
    public ResponseEntity<GoalResponseDTO> getByName(@PathVariable(name = "title") String title) {
        GoalResponseDTO goal = goalService.showByTitle(title);
        return ResponseEntity.ok(goal);
    }

    @PostMapping
    public ResponseEntity<GoalResponseDTO> create(@RequestBody @Valid GoalRequestDTO dto) {
        GoalResponseDTO saved = goalService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<GoalResponseDTO> update(@PathVariable(name = "id") Long id, @RequestBody @Valid GoalRequestDTO dto) {
        GoalResponseDTO toggled = goalService.update(id, dto);
        return ResponseEntity.ok(toggled);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable(name = "id") Long id) {
        goalService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
