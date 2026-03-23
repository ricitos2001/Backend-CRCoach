package org.example.backendcrcoach.services;

import jakarta.transaction.Transactional;
import org.example.backendcrcoach.domain.dto.ArenaRequestDTO;
import org.example.backendcrcoach.domain.dto.ArenaResponseDTO;
import org.example.backendcrcoach.domain.entities.Arena;
import org.example.backendcrcoach.mappers.ArenaMapper;
import org.example.backendcrcoach.repositories.ArenaRepository;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ArenaService {

    private final ArenaRepository arenaRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ArenaService(ArenaRepository arenaRepository) {
        this.arenaRepository = arenaRepository;
    }

    public ArenaResponseDTO createArena(ArenaRequestDTO dto) {
        Arena arena = ArenaMapper.toEntity(dto);
        Arena saved = arenaRepository.save(arena);
        return ArenaMapper.toDTO(saved);
    }

    public List<ArenaResponseDTO> getAllArenas() {
        return arenaRepository.findAll().stream().map(ArenaMapper::toDTO).collect(Collectors.toList());
    }

    public Optional<ArenaResponseDTO> getArenaById(Long id) {
        return arenaRepository.findById(id).map(ArenaMapper::toDTO);
    }

    public Optional<ArenaResponseDTO> updateArena(Long id, ArenaRequestDTO dto) {
        return arenaRepository.findById(id).map(existing -> {
            Arena updated = ArenaMapper.toEntity(dto);
            updated.setId(existing.getId());
            Arena saved = arenaRepository.save(updated);
            return ArenaMapper.toDTO(saved);
        });
    }

    public void deleteArena(Long id) {
        arenaRepository.deleteById(id);
    }

    public Arena resolveArenaFromNode(JsonNode arenaNode) {
        if (arenaNode == null || arenaNode.isNull()) return null;
        String rawName = readText(arenaNode, "name");
        if (rawName == null || rawName.isBlank()) return null;

        Arena arena = arenaRepository.findByRawName(rawName)
                .or(() -> arenaRepository.findByName(rawName))
                .orElseGet(Arena::new);

        arena.setRawName(rawName);
        arena.setName(rawName);
        return arenaRepository.save(arena);
    }

    private String readText(JsonNode json, String field) {
        JsonNode node = json.get(field);
        return node == null || node.isNull() ? null : objectMapper.convertValue(node, String.class);
    }
}

