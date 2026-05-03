package org.example.backendcrcoach.services;

import jakarta.transaction.Transactional;
import org.example.backendcrcoach.web.exceptions.DuplicatedUserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.example.backendcrcoach.config.WebClientHelper;
import org.example.backendcrcoach.services.CardClassifierService;
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
    private static final Logger log = LoggerFactory.getLogger(BattleService.class);

    private final BattleRepository battleRepository;
    private final PlayerEntityRepository playerEntityRepository;
    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ArenaService arenaService;
    private final ClanService clanService;
    private final GameModeService gameModeService;
    private final WebClientHelper webClientHelper;
    private final DeckService deckService;
    private final CardClassifierService cardClassifierService;
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
            CardClassifierService cardClassifierService,
            ApplicationContext applicationContext) {
        this.battleRepository = battleRepository;
        this.playerEntityRepository = playerEntityRepository;
        this.webClient = builder
                .baseUrl(API_URL)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + API_KEY)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.USER_AGENT, "CRCoach/Backend-CRCoach")                .build();
        this.arenaService = arenaService;
        this.clanService = clanService;
        this.gameModeService = gameModeService;
        this.webClientHelper = webClientHelper;
        this.deckService = deckService;
        this.cardClassifierService = cardClassifierService;
        this.applicationContext = applicationContext;
    }

    public BattleResponseDTO createBattle(BattleRequestDTO dto) {
        Battle battle = BattleMapper.toEntity(dto);
        if (dto.getTeam() != null && dto.getTeam().getTag() != null) {
            playerEntityRepository.findByTag(dto.getTeam().getTag()).ifPresent(battle::setTeam);
        }
        if (dto.getOpponent() != null && dto.getOpponent().getTag() != null) {
            playerEntityRepository.findByTag(dto.getOpponent().getTag()).ifPresent(battle::setOpponent);
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
                playerEntityRepository.findByTag(dto.getTeam().getTag()).ifPresent(updated::setTeam);
            } else {
                updated.setTeam(existing.getTeam());
            }

            if (dto.getOpponent() != null && dto.getOpponent().getTag() != null) {
                playerEntityRepository.findByTag(dto.getOpponent().getTag()).ifPresent(updated::setOpponent);
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
     * Obtiene el registro de batallas donde aparece el jugador identificado por su tag
     * (puede aparecer en team u opponent). Devuelve las batallas más recientes primero.
     * Si limit es null o <=0 se usa un valor por defecto de 50.
     */
    public List<BattleResponseDTO> getBattlesByPlayerTag(String playerTag, Integer limit) {
        if (playerTag == null || playerTag.isBlank()) return List.of();
        int pageSize = (limit == null || limit <= 0) ? 50 : limit;
        Pageable pageable = PageRequest.of(0, pageSize);
        List<Battle> battles = battleRepository.findByTeamTagOrOpponentTagOrderByBattleTimeDesc(playerTag, playerTag, pageable);
        return battles.stream().map(BattleMapper::toDTO).collect(Collectors.toList());
    }

    /**
     * Importa batallas de la API de Clash Royale para un jugador (tag sin '#').
     * Evita duplicados basándose en el campo battleTime.
     * Devuelve el DTO de la última batalla guardada (o null si no se importó ninguna).
     */
    public BattleResponseDTO importBattlesForPlayer(String playerTag) {
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

        Battle savedBattle = null;
        for (JsonNode node : battlesJson) {
            Battle persistedOrExisting = mapApiResponseToEntity(node);
            if (persistedOrExisting == null) continue;
            savedBattle = persistedOrExisting;
        }

        if (savedBattle == null) {
            return null;
        }
        return BattleMapper.toDTO(savedBattle);
    }

    private Battle mapApiResponseToEntity(JsonNode json) {
        String battleTime = readText(json, "battleTime");
        if (battleTime == null) return null;
        if (battleRepository.existsByBattleTime(battleTime)) {
            return null;
        }

        Battle battle = new Battle();
        battle.setType(readText(json, "type"));
        battle.setBattleTime(battleTime);
        battle.setIsLadderTournament(readBoolean(json, "isLadderTournament"));
        battle.setDeckSelection(readText(json, "deckSelection"));
        battle.setIsHostedMatch(readBoolean(json, "isHostedMatch"));
        battle.setLeagueNumber(readInteger(json, "leagueNumber"));
        battle.setArena(arenaService.resolveArenaFromNode(json.get("arena")));
        battle.setGameMode(gameModeService.resolveGameModeFromNode(json.get("gameMode")));
        battle.setTeam(resolvePlayerEntityFromArray(json.get("team")));
        battle.setOpponent(resolvePlayerEntityFromArray(json.get("opponent")));
        return battleRepository.save(battle);
    }

    private PlayerEntity resolvePlayerEntityFromArray(JsonNode playersNode) {
        if (playersNode == null || !playersNode.isArray() || playersNode.isEmpty()) return null;

        JsonNode node = playersNode.get(0);
        String rawTag = readText(node, "tag");
        if (rawTag == null || rawTag.isBlank()) return null;

        PlayerEntity entity = new PlayerEntity();

        entity.setTag(rawTag);
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
            // Clasificar el tipo de uso (normal/evolution/hero) usando metadata del catálogo si procede
            try {
                card.setUseType(cardClassifierService.classify(card));
            } catch (Exception ex) {
                // No bloquear el parsing por un fallo en la clasificación
                log.debug("Error clasificando carta {}: {}", card.getCardId(), ex.getMessage());
            }

            cards.add(card);
        }

        deck.setPlayerCards(cards);
        // El arquetipo se calcula en DeckService al persistir el deck
        return deck;
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
