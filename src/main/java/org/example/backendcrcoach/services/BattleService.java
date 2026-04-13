package org.example.backendcrcoach.services;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.example.backendcrcoach.config.WebClientHelper;
import org.example.backendcrcoach.domain.dto.BattleRequestDTO;
import org.example.backendcrcoach.domain.dto.BattleResponseDTO;
import org.example.backendcrcoach.domain.entities.Battle;
import org.example.backendcrcoach.domain.entities.Deck;
import org.example.backendcrcoach.domain.entities.IconUrl;
import org.example.backendcrcoach.domain.entities.PlayerCard;
import org.example.backendcrcoach.domain.entities.PlayerEntity;
import org.example.backendcrcoach.mappers.BattleMapper;
import org.example.backendcrcoach.repositories.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class BattleService {
    private static final String TAG_PREFIX = "#";
    private static final Logger log = LoggerFactory.getLogger(BattleService.class);

    private final BattleRepository battleRepository;
    private final PlayerProfileRepository playerProfileRepository;
    private final PlayerEntityRepository playerEntityRepository;
    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ArenaService arenaService;
    private final ClanService clanService;
    private final GameModeService gameModeService;
    private final WebClientHelper webClientHelper;
    private final DeckService deckService;
    private final ApplicationContext applicationContext;

    public BattleService(
            BattleRepository battleRepository,
            PlayerProfileRepository playerProfileRepository,
            PlayerEntityRepository playerEntityRepository,
            WebClient.Builder builder,
            @Value("${clash.royale.api.url}") String API_URL,
            @Value("${clash.royale.api.key}") String API_KEY,
            ArenaService arenaService, ClanService clanService,
            GameModeService gameModeService,
            WebClientHelper webClientHelper,
            DeckService deckService,
            ApplicationContext applicationContext) {
        this.battleRepository = battleRepository;
        this.playerProfileRepository = playerProfileRepository;
        this.playerEntityRepository = playerEntityRepository;
        this.webClient = builder
                .baseUrl(API_URL)
                .defaultHeader("Authorization", "Bearer " + API_KEY)
                .build();
        this.arenaService = arenaService;
        this.clanService = clanService;
        this.gameModeService = gameModeService;
        this.webClientHelper = webClientHelper;
        this.deckService = deckService;
        this.applicationContext = applicationContext;
    }

    @Async("taskExecutor")
    public void importBattlesForPlayerAsync(String playerTag) {
        try {
            // Call through proxy to ensure transactional boundaries are applied to the import
            BattleService self = applicationContext.getBean(BattleService.class);
            int imported = self.importBattlesForPlayer(playerTag);
            log.info("Imported {} battles for player {} (async)", imported, playerTag);
        } catch (Exception e) {
            log.error("Error importing battles for player {} asynchronously: {}", playerTag, e.getMessage(), e);
        }
    }

    public BattleResponseDTO createBattle(BattleRequestDTO dto) {
        Battle battle = BattleMapper.toEntity(dto);
        if (dto.getTeam() != null && dto.getTeam().getTag() != null) {
            playerEntityRepository.findByTag(normalizeTag(dto.getTeam().getTag())).ifPresent(battle::setTeam);
        }
        if (dto.getOpponent() != null && dto.getOpponent().getTag() != null) {
            playerEntityRepository.findByTag(normalizeTag(dto.getOpponent().getTag())).ifPresent(battle::setOpponent);
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
            if (dto.getTeam() != null && dto.getTeam().getTag() != null) {
                playerEntityRepository.findByTag(normalizeTag(dto.getTeam().getTag())).ifPresent(updated::setTeam);
            } else {
                updated.setTeam(existing.getTeam());
            }

            if (dto.getOpponent() != null && dto.getOpponent().getTag() != null) {
                playerEntityRepository.findByTag(normalizeTag(dto.getOpponent().getTag())).ifPresent(updated::setOpponent);
            } else {
                updated.setOpponent(existing.getOpponent());
            }

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
        String responseBody = webClientHelper.fetchGetWithRetries(webClient, "/players/{tag}/battlelog", playerTag);
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
        battle.setArena(arenaService.resolveArenaFromNode(json.get("arena")));
        // Resolve gameMode node into persisted GameMode entity (if present)
        battle.setGameMode(gameModeService.resolveGameModeFromNode(json.get("gameMode")));
        battle.setTeam(resolvePlayerEntityFromArray(json.get("team")));
        battle.setOpponent(resolvePlayerEntityFromArray(json.get("opponent")));

        // Note: do not set battleTimeTs (field removed). We keep battleTime as the source of truth.

        return battle;
    }

    private PlayerEntity resolvePlayerEntityFromArray(JsonNode playersNode) {
        if (playersNode == null || !playersNode.isArray() || playersNode.isEmpty()) return null;

        JsonNode node = playersNode.get(0);
        String rawTag = readText(node, "tag");
        if (rawTag == null || rawTag.isBlank()) return null;

        String normalizedTag = normalizeTag(rawTag);
        PlayerEntity entity = playerEntityRepository.findByTag(normalizedTag).orElseGet(PlayerEntity::new);

        entity.setTag(normalizedTag);
        entity.setName(readText(node, "name"));
        entity.setStartingTrophies(readInteger(node, "startingTrophies"));
        entity.setTrophyChange(readInteger(node, "trophyChange"));
        entity.setCrowns(readInteger(node, "crowns"));
        entity.setKingTowerHitPoints(readInteger(node, "kingTowerHitPoints"));
        entity.setGlobalRank(readInteger(node, "globalRank"));
        entity.setElixirLeaked(readDouble(node, "elixirLeaked"));
        entity.setClan(clanService.resolveClanFromNode(node.get("clan")));

        JsonNode princessNode = node.get("princessTowersHitPoints");
        if (princessNode != null && princessNode.isArray()) {
            List<Integer> hp = new ArrayList<>();
            for (JsonNode n : princessNode) {
                if (!n.isNull()) hp.add(n.asInt());
            }
            entity.setPrincessTowersHitPoints(hp);
        }

        Deck deck = resolveDeckFromArray(node.get("cards"));
        // Persistir el deck si es necesario para que tenga id y arquetipo calculado
        Deck persisted = deckService.persistDeckIfNeeded(deck);
        entity.setPlayerDeck(persisted);

        return playerEntityRepository.save(entity);
    }

    private Deck resolveDeckFromArray(JsonNode cardsNode) {
        if (cardsNode == null || !cardsNode.isArray() || cardsNode.isEmpty()) return null;

        Deck deck = new Deck();
        List<PlayerCard> cards = new ArrayList<>();

        for (JsonNode c : cardsNode) {
            PlayerCard card = new PlayerCard();
            card.setCardId(readInteger(c, "id"));
            card.setName(readText(c, "name"));
            card.setLevel(readInteger(c, "level"));
            card.setMaxLevel(readInteger(c, "maxLevel"));
            card.setMaxEvolutionLevel(readInteger(c, "maxEvolutionLevel"));
            card.setRarity(readText(c, "rarity"));
            card.setElixirCost(readInteger(c, "elixirCost"));
            card.setSupportCard(false);

            JsonNode iconNode = c.get("iconUrls");
            if (iconNode != null && !iconNode.isNull()) {
                IconUrl icon = new IconUrl();
                icon.setMedium(readText(iconNode, "medium"));
                icon.setEvolutionMedium(readText(iconNode, "evolutionMedium"));
                card.setIconUrl(icon);
            }

            cards.add(card);
        }

        deck.setPlayerCards(cards);
        // El arquetipo se calcula en DeckService al persistir el deck
        return deck;
    }

    private String normalizeTag(String rawTag) {
        if (rawTag == null || rawTag.isBlank()) return rawTag;
        return rawTag.startsWith(TAG_PREFIX) ? rawTag : TAG_PREFIX + rawTag;
    }

    private String readText(JsonNode json, String field) {
        JsonNode node = json.get(field);
        return node == null || node.isNull() ? null : objectMapper.convertValue(node, String.class);
    }

    private Integer readInteger(JsonNode json, String field) {
        JsonNode node = json.get(field);
        return node == null || node.isNull() ? null : node.asInt();
    }

    private Double readDouble(JsonNode json, String field) {
        JsonNode node = json.get(field);
        return node == null || node.isNull() ? null : node.asDouble();
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
