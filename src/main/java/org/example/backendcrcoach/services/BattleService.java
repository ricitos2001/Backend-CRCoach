package org.example.backendcrcoach.services;

import jakarta.transaction.Transactional;
import org.example.backendcrcoach.domain.dto.BattleRequestDTO;
import org.example.backendcrcoach.domain.dto.BattleResponseDTO;
import org.example.backendcrcoach.domain.entities.Battle;
import org.example.backendcrcoach.mappers.BattleMapper;
import org.example.backendcrcoach.repositories.BattleRepository;
import org.example.backendcrcoach.repositories.PlayerProfileRepository;
import org.example.backendcrcoach.domain.entities.PlayerProfile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class BattleService {

    private final BattleRepository battleRepository;
    private final PlayerProfileRepository playerProfileRepository;
    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public BattleService(
            BattleRepository battleRepository,
            PlayerProfileRepository playerProfileRepository,
            WebClient.Builder builder,
            @Value("${clash.royale.api.url}") String API_URL,
            @Value("${clash.royale.api.key}") String API_KEY
    ) {
        this.battleRepository = battleRepository;
        this.playerProfileRepository = playerProfileRepository;
        this.webClient = builder
                .baseUrl(API_URL)
                .defaultHeader("Authorization", "Bearer " + API_KEY)
                .build();
    }

    public BattleResponseDTO createBattle(BattleRequestDTO dto) {
        Battle battle = BattleMapper.toEntity(dto);
        // Si el DTO incluye playerTag, intentar enlazar al PlayerProfile existente
        if (dto.getPlayerTag() != null) {
            playerProfileRepository.findByTag(dto.getPlayerTag()).ifPresent(battle::setPlayerProfile);
        }
        Battle saved = battleRepository.save(battle);
        return BattleMapper.toDTO(saved);
    }

    public List<BattleResponseDTO> getAllBattles() {
        return battleRepository.findAll()
                .stream()
                .map(BattleMapper::toDTO)
                .collect(Collectors.toList());
    }

    public Optional<BattleResponseDTO> getBattleById(Long id) {
        return battleRepository.findById(id).map(BattleMapper::toDTO);
    }

    public Optional<BattleResponseDTO> updateBattle(Long id, BattleRequestDTO dto) {
        return battleRepository.findById(id).map(existing -> {
            Battle updated = BattleMapper.toEntity(dto);
            updated.setId(existing.getId());
            Battle saved = battleRepository.save(updated);
            return BattleMapper.toDTO(saved);
        });
    }

    public void deleteBattle(Long id) {
        battleRepository.deleteById(id);
    }

    /**
     * Importa batallas de la API de Clash Royale para un jugador (tag sin '#').
     * Evita duplicados basándose en el campo battleTime.
     * Devuelve el número de batallas importadas.
     */
    public int importBattlesForPlayer(String playerTag) {
        String responseBody = webClient.get()
                .uri("/players/{tag}/battlelog", "#" + playerTag)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        if (responseBody == null || responseBody.isBlank()) {
            throw new IllegalArgumentException("No se pudo obtener batallas para el jugador con tag: " + playerTag);
        }

        JsonNode battlesJson;
        try {
            battlesJson = objectMapper.readTree(responseBody);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("Respuesta invalida al obtener batallas para el jugador con tag: " + playerTag, e);
        }

        if (!battlesJson.isArray()) {
            throw new IllegalArgumentException("Formato inesperado: se esperaba un array de batallas");
        }

        int imported = 0;
        for (JsonNode node : battlesJson) {
            Battle battle = mapApiResponseToEntity(node);
            // Evitar duplicados basándose en el campo 'battleTime' (más fiable que comparar JSON)
            if (battle.getBattleTime() == null) continue;
            if (battleRepository.existsByBattleTime(battle.getBattleTime())) continue;

            // Enlazar al PlayerProfile por tag si existe
            playerProfileRepository.findByTag("#" + playerTag).ifPresent(battle::setPlayerProfile);

            battleRepository.save(battle);
            imported++;
        }

        return imported;
    }

    private Battle mapApiResponseToEntity(JsonNode json) {
        Battle battle = new Battle();
        battle.setType(readText(json, "type"));
        battle.setBattleTime(readText(json, "battleTime"));
        battle.setIsLadderTournament(readBoolean(json, "isLadderTournament"));
        battle.setDeckSelection(readText(json, "deckSelection"));
        battle.setIsHostedMatch(readBoolean(json, "isHostedMatch"));
        battle.setLeagueNumber(readInteger(json, "leagueNumber"));

        battle.setArena(readJsonText(json, "arena"));
        battle.setGameMode(readJsonText(json, "gameMode"));
        battle.setTeam(readJsonText(json, "team"));
        battle.setOpponent(readJsonText(json, "opponent"));

        return battle;
    }

    private String readText(JsonNode json, String field) {
        JsonNode node = json.get(field);
        return node == null || node.isNull() ? null : objectMapper.convertValue(node, String.class);
    }

    private Integer readInteger(JsonNode json, String field) {
        JsonNode node = json.get(field);
        return node == null || node.isNull() ? null : node.asInt();
    }

    private Boolean readBoolean(JsonNode json, String field) {
        JsonNode node = json.get(field);
        return node == null || node.isNull() ? null : node.asBoolean();
    }

    private String readJsonText(JsonNode json, String field) {
        JsonNode node = json.get(field);
        return node == null || node.isNull() ? null : node.toString();
    }
}

