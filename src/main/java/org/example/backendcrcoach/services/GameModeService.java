package org.example.backendcrcoach.services;

import jakarta.transaction.Transactional;
import org.example.backendcrcoach.domain.dto.GameModeRequestDTO;
import org.example.backendcrcoach.domain.dto.GameModeResponseDTO;
import org.example.backendcrcoach.domain.entities.GameMode;
import org.example.backendcrcoach.mappers.GameModeMapper;
import org.example.backendcrcoach.repositories.GameModeRepository;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class GameModeService {

    private final GameModeRepository gameModeRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GameModeService(GameModeRepository gameModeRepository) {
        this.gameModeRepository = gameModeRepository;
    }

    public GameModeResponseDTO createGameMode(GameModeRequestDTO dto) {
        GameMode gm = GameModeMapper.toEntity(dto);
        GameMode saved = gameModeRepository.save(gm);
        return GameModeMapper.toDTO(saved);
    }

    public List<GameModeResponseDTO> getAllGameModes() {
        return gameModeRepository.findAll().stream().map(GameModeMapper::toDTO).collect(Collectors.toList());
    }

    public Optional<GameModeResponseDTO> getGameModeById(Long id) {
        return gameModeRepository.findById(id).map(GameModeMapper::toDTO);
    }

    public Optional<GameModeResponseDTO> updateGameMode(Long id, GameModeRequestDTO dto) {
        return gameModeRepository.findById(id).map(existing -> {
            GameMode updated = GameModeMapper.toEntity(dto);
            updated.setId(existing.getId());
            GameMode saved = gameModeRepository.save(updated);
            return GameModeMapper.toDTO(saved);
        });
    }

    public void deleteGameMode(Long id) {
        gameModeRepository.deleteById(id);
    }

    /**
     * Resuelve o crea un GameMode a partir de un nodo JSON (estructura recibida de la API externa).
     * Busca por gameModeId o por nombre.
     */
    public GameMode resolveGameModeFromNode(JsonNode gmNode) {
        if (gmNode == null || gmNode.isNull()) return null;

        Integer gmId = null;
        JsonNode idNode = gmNode.get("id");
        if (idNode != null && !idNode.isNull()) gmId = idNode.asInt();

        String name = null;
        JsonNode nameNode = gmNode.get("name");
        if (nameNode != null && !nameNode.isNull()) name = objectMapper.convertValue(nameNode, String.class);

        if ((gmId == null || gmId == 0) && (name == null || name.isBlank())) return null;

        GameMode gm = null;
        if (gmId != null && gmId != 0) {
            gm = gameModeRepository.findByGameModeId(gmId).orElse(null);
        }
        if (gm == null && name != null && !name.isBlank()) {
            gm = gameModeRepository.findByName(name).orElseGet(GameMode::new);
        }

        if (gm == null) gm = new GameMode();
        if (gmId != null && gmId != 0) gm.setGameModeId(gmId);
        if (name != null && !name.isBlank()) gm.setName(name);

        return gameModeRepository.save(gm);
    }
}

