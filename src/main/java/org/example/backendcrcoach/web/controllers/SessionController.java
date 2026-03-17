package org.example.backendcrcoach.web.controllers;

import jakarta.validation.Valid;
import org.example.backendcrcoach.domain.dto.SessionRequestDTO;
import org.example.backendcrcoach.domain.dto.SessionResponseDTO;
import org.example.backendcrcoach.services.SessionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/v1/sessions", produces = MediaType.APPLICATION_JSON_VALUE)
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @GetMapping
    public ResponseEntity<Page<SessionResponseDTO>> list(Pageable pageable) {
        Page<SessionResponseDTO> session = sessionService.list(pageable);
        return ResponseEntity.ok(session);
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<SessionResponseDTO> getById(@PathVariable(name = "id") Long id) {
        SessionResponseDTO session = sessionService.showById(id);
        return ResponseEntity.ok(session);
    }

    @GetMapping("/title/{title}")
    public ResponseEntity<SessionResponseDTO> getByName(@PathVariable(name = "title") String title) {
        SessionResponseDTO session = sessionService.showByTitle(title);
        return ResponseEntity.ok(session);
    }

    @PostMapping
    public ResponseEntity<SessionResponseDTO> create(@RequestBody @Valid SessionRequestDTO dto) {
        SessionResponseDTO saved = sessionService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SessionResponseDTO> update(@PathVariable(name = "id") Long id, @RequestBody @Valid SessionRequestDTO dto) {
        SessionResponseDTO toggled = sessionService.update(id, dto);
        return ResponseEntity.ok(toggled);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable(name = "id") Long id) {
        sessionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
