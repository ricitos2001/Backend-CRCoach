package org.example.backendcrcoach.services;

import jakarta.transaction.Transactional;
import org.example.backendcrcoach.domain.dto.PlayerProfileRequestDTO;
import org.example.backendcrcoach.domain.dto.PlayerProfileResponseDTO;
// ...existing code...
import org.example.backendcrcoach.domain.entities.PlayerProfile;
import org.example.backendcrcoach.mappers.PlayerProfileMapper;
// ...existing code...
import org.example.backendcrcoach.repositories.PlayerProfileRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.example.backendcrcoach.config.WebClientHelper;
import org.springframework.web.bind.annotation.RequestBody;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.example.backendcrcoach.domain.entities.PlayerCard;
import org.example.backendcrcoach.domain.dto.DeckRequestDTO;
import org.example.backendcrcoach.mappers.DeckMapper;

import java.util.ArrayList;
import java.util.List;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
@Transactional
public class PlayerProfileService {

    private final PlayerProfileRepository playerProfileRepository;
    private final SnapshotService snapshotService;
    private final PlayerCardService playerCardService;
    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ArenaService arenaService;
    private final ClanService clanService;
    private final DeckService deckService;
    private final WebClientHelper webClientHelper;
    private final LeagueStadisticService leagueStadisticService;

    public PlayerProfileService(
            PlayerProfileRepository playerProfileRepository,
            SnapshotService snapshotService,
            WebClient.Builder builder,
            @Value("${clash.royale.api.url}") String API_URL,
            @Value("${clash.royale.api.key}") String API_KEY,
            PlayerCardService playerCardService,
            ArenaService arenaService,
            ClanService clanService,
            DeckService deckService, WebClientHelper webClientHelper, LeagueStadisticService leagueStadisticService) {
        this.playerProfileRepository = playerProfileRepository;
        this.snapshotService = snapshotService;
        this.webClient = builder
                .baseUrl(API_URL)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + API_KEY)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.USER_AGENT, "CRCoach/Backend-CRCoach")
                .build();
        this.playerCardService = playerCardService;
        this.arenaService = arenaService;
        this.clanService = clanService;
        this.deckService = deckService;
        this.webClientHelper = webClientHelper;
        this.leagueStadisticService = leagueStadisticService;
    }

    public Page<PlayerProfileResponseDTO> list(Pageable pageable) {
        Page<PlayerProfile> page = playerProfileRepository.findAll(pageable);
        // Inicializar colecciones lazy antes de mapear a DTOs para evitar LazyInitializationException
        page.forEach(this::initializeCollections);
        return page.map(PlayerProfileMapper::toDTO);
    }

    public PlayerProfileResponseDTO showById(Long id) {
        PlayerProfile profile = playerProfileRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("PlayerProfile no encontrado con id: " + id));
        // Inicializar colecciones lazy mientras la transacción está abierta
        initializeCollections(profile);
        return PlayerProfileMapper.toDTO(profile);
    }

    public PlayerProfileResponseDTO showByTag(String playerTag) {
        PlayerProfile profile = playerProfileRepository.findByTag(playerTag).orElseThrow(() -> new IllegalArgumentException("PlayerProfile no encontrado con tag: " + playerTag));
        // Inicializar colecciones lazy mientras la transacción está abierta
        initializeCollections(profile);
        return PlayerProfileMapper.toDTO(profile);
    }

    /**
     * Fuerza la inicialización de colecciones lazy que luego son serializadas
     * (por ejemplo Deck.playerCards y listas de PlayerCard) para evitar
     * LazyInitializationException al serializar fuera de la sesión de Hibernate.
     */
    private void initializeCollections(PlayerProfile profile) {
        if (profile == null) return;
        try {
            if (profile.getPlayerCards() != null) profile.getPlayerCards().size();
            if (profile.getSupportCards() != null) profile.getSupportCards().size();
            if (profile.getCurrentDeck() != null && profile.getCurrentDeck().getPlayerCards() != null)
                profile.getCurrentDeck().getPlayerCards().size();
            if (profile.getCurrentDeckSupportCards() != null && profile.getCurrentDeckSupportCards().getPlayerCards() != null)
                profile.getCurrentDeckSupportCards().getPlayerCards().size();
            // Inicializar carta favorita si existe
            if (profile.getCurrentFavouriteCard() != null) {
                // acceder a un campo simple forzará inicialización si fuera lazy proxy
                profile.getCurrentFavouriteCard().getCardId();
            }
        } catch (Exception ex) {
            // No propagamos la excepción de inicialización; el objetivo es intentar evitar la LazyInitializationException
        }
    }
    public PlayerProfileResponseDTO create(PlayerProfileRequestDTO dto) {
        if (dto.getTag() != null && playerProfileRepository.existsByTag(dto.getTag())) {
            throw new IllegalArgumentException("Ya existe un PlayerProfile con tag: " + dto.getTag());
        }
        PlayerProfile profile = PlayerProfileMapper.toEntity(dto);
        // Persistir mazos si vienen en el DTO para evitar referencias transientes
        profile.setCurrentDeck(deckService.persistDeckIfNeeded(profile.getCurrentDeck()));
        profile.setCurrentDeckSupportCards(deckService.persistDeckIfNeeded(profile.getCurrentDeckSupportCards()));
        // Si hay carta favorita en el DTO, la guardamos después de persistir el perfil
        PlayerCard favCard = profile.getCurrentFavouriteCard();
        profile.setCurrentFavouriteCard(null);
        PlayerProfile saved = playerProfileRepository.save(profile);
        if (favCard != null) {
            PlayerCard savedFav = playerCardService.persistFavouriteCardIfNeeded(saved, favCard);
            saved.setCurrentFavouriteCard(savedFav);
            saved = playerProfileRepository.save(saved);
        }
        return PlayerProfileMapper.toDTO(saved);
    }

    public PlayerProfileResponseDTO update(Long id, @RequestBody PlayerProfileRequestDTO dto) {
        PlayerProfile profile = playerProfileRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("PlayerProfile no encontrado con id: " + id));
        if (dto.getTag() != null && !dto.getTag().equals(profile.getTag()) && playerProfileRepository.existsByTag(dto.getTag())) {
            throw new IllegalArgumentException("Ya existe un PlayerProfile con tag: " + dto.getTag());
        }
        updateBasicFields(dto, profile);
        // Persistir mazos si fueron cambiados para evitar referencias transientes
        profile.setCurrentDeck(deckService.persistDeckIfNeeded(profile.getCurrentDeck()));
        profile.setCurrentDeckSupportCards(deckService.persistDeckIfNeeded(profile.getCurrentDeckSupportCards()));
        // Persistir carta favorita si fue cambiada
        profile.setCurrentFavouriteCard(playerCardService.persistFavouriteCardIfNeeded(profile, profile.getCurrentFavouriteCard()));
        PlayerProfile saved = playerProfileRepository.save(profile);
        return PlayerProfileMapper.toDTO(saved);
    }

    private void updateBasicFields(PlayerProfileRequestDTO dto, PlayerProfile profile) {
        Optional.ofNullable(dto.getTag()).ifPresent(profile::setTag);
        Optional.ofNullable(dto.getName()).ifPresent(profile::setName);
        Optional.ofNullable(dto.getExpLevel()).ifPresent(profile::setExpLevel);
        Optional.ofNullable(dto.getTrophies()).ifPresent(profile::setTrophies);
        Optional.ofNullable(dto.getBestTrophies()).ifPresent(profile::setBestTrophies);
        Optional.ofNullable(dto.getWins()).ifPresent(profile::setWins);
        Optional.ofNullable(dto.getLosses()).ifPresent(profile::setLosses);
        Optional.ofNullable(dto.getBattleCount()).ifPresent(profile::setBattleCount);
        Optional.ofNullable(dto.getThreeCrownWins()).ifPresent(profile::setThreeCrownWins);
        Optional.ofNullable(dto.getChallengeCardsWon()).ifPresent(profile::setChallengeCardsWon);
        Optional.ofNullable(dto.getChallengeMaxWins()).ifPresent(profile::setChallengeMaxWins);
        Optional.ofNullable(dto.getTournamentCardsWon()).ifPresent(profile::setTournamentCardsWon);
        Optional.ofNullable(dto.getTournamentBattleCount()).ifPresent(profile::setTournamentBattleCount);
        Optional.ofNullable(dto.getRole()).ifPresent(profile::setRole);
        Optional.ofNullable(dto.getDonations()).ifPresent(profile::setDonations);
        Optional.ofNullable(dto.getDonationsReceived()).ifPresent(profile::setDonationsReceived);
        Optional.ofNullable(dto.getTotalDonations()).ifPresent(profile::setTotalDonations);
        Optional.ofNullable(dto.getWarDayWins()).ifPresent(profile::setWarDayWins);
        Optional.ofNullable(dto.getClanCardsCollected()).ifPresent(profile::setClanCardsCollected);
        Optional.ofNullable(dto.getStarPoints()).ifPresent(profile::setStarPoints);
        Optional.ofNullable(dto.getExpPoints()).ifPresent(profile::setExpPoints);
        Optional.ofNullable(dto.getLegacyTrophyRoadHighScore()).ifPresent(profile::setLegacyTrophyRoadHighScore);
        Optional.ofNullable(dto.getTotalExpPoints()).ifPresent(profile::setTotalExpPoints);

        Optional.ofNullable(dto.getClan()).ifPresent(profile::setClan);
        Optional.ofNullable(dto.getArena()).ifPresent(profile::setArena);
        Optional.ofNullable(dto.getLeagueStatistics()).ifPresent(profile::setLeagueStatistics);
        Optional.ofNullable(dto.getBadges()).ifPresent(profile::setBadges);
        Optional.ofNullable(dto.getAchievements()).ifPresent(profile::setAchievements);
        Optional.ofNullable(dto.getPlayerCards()).ifPresent(profile::setPlayerCards);
        Optional.ofNullable(dto.getSupportCards()).ifPresent(profile::setSupportCards);
        Optional.ofNullable(dto.getCurrentDeck()).ifPresent(profile::setCurrentDeck);
        Optional.ofNullable(dto.getCurrentDeckSupportCards()).ifPresent(profile::setCurrentDeckSupportCards);
        Optional.ofNullable(dto.getCurrentFavouriteCard()).ifPresent(profile::setCurrentFavouriteCard);
        Optional.ofNullable(dto.getCurrentPathOfLegendSeasonResult()).ifPresent(profile::setCurrentPathOfLegendSeasonResult);
        Optional.ofNullable(dto.getLastPathOfLegendSeasonResult()).ifPresent(profile::setLastPathOfLegendSeasonResult);
        Optional.ofNullable(dto.getBestPathOfLegendSeasonResult()).ifPresent(profile::setBestPathOfLegendSeasonResult);
        Optional.ofNullable(dto.getProgress()).ifPresent(profile::setProgress);
    }

    public void delete(Long id) {
        if (!playerProfileRepository.existsById(id)) {
            throw new IllegalArgumentException("PlayerProfile no encontrado con id: " + id);
        }
        playerProfileRepository.deleteById(id);
    }

    public PlayerProfileResponseDTO getPlayer(String playerTag) {
        String responseBody = webClientHelper.fetchGetWithRetries(webClient, "/players/{tag}", playerTag);
        if (responseBody == null || responseBody.isBlank()) {
            throw new IllegalArgumentException("No se pudo obtener el perfil del jugador con tag: " + playerTag);
        }
        JsonNode playerJson;
        try {
            playerJson = objectMapper.readTree(responseBody);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("Respuesta invalida al obtener el perfil del jugador con tag: " + playerTag, e);
        }
        PlayerProfile playerProfile = mapApiResponseToEntity(playerJson);
        PlayerProfile savedProfile;
        PlayerProfile existing = playerProfileRepository.findByTag(playerProfile.getTag()).orElse(null);
        boolean statsChanged;
        if (existing != null) {
            // Determinar si las estadísticas relevantes cambiaron
            statsChanged = hasSnapshotRelevantChanges(existing, playerProfile);
            // Actualizar campos del perfil existente con la información nueva
            existing.setName(playerProfile.getName());
            existing.setExpLevel(playerProfile.getExpLevel());
            existing.setTrophies(playerProfile.getTrophies());
            existing.setBestTrophies(playerProfile.getBestTrophies());
            existing.setWins(playerProfile.getWins());
            existing.setLosses(playerProfile.getLosses());
            existing.setBattleCount(playerProfile.getBattleCount());
            existing.setThreeCrownWins(playerProfile.getThreeCrownWins());
            existing.setChallengeCardsWon(playerProfile.getChallengeCardsWon());
            existing.setChallengeMaxWins(playerProfile.getChallengeMaxWins());
            existing.setTournamentCardsWon(playerProfile.getTournamentCardsWon());
            existing.setTournamentBattleCount(playerProfile.getTournamentBattleCount());
            existing.setRole(playerProfile.getRole());
            existing.setDonations(playerProfile.getDonations());
            existing.setDonationsReceived(playerProfile.getDonationsReceived());
            existing.setTotalDonations(playerProfile.getTotalDonations());
            existing.setWarDayWins(playerProfile.getWarDayWins());
            existing.setClanCardsCollected(playerProfile.getClanCardsCollected());
            existing.setStarPoints(playerProfile.getStarPoints());
            existing.setExpPoints(playerProfile.getExpPoints());
            existing.setLegacyTrophyRoadHighScore(playerProfile.getLegacyTrophyRoadHighScore());
            existing.setTotalExpPoints(playerProfile.getTotalExpPoints());
            existing.setClan(playerProfile.getClan());
            existing.setArena(playerProfile.getArena());
            existing.setLeagueStatistics(playerProfile.getLeagueStatistics());
            existing.setBadges(playerProfile.getBadges());
            existing.setAchievements(playerProfile.getAchievements());
            // Reasignar el playerProfile en las PlayerCard traidas desde la API
            // para que apunten al entity gestionado (existing) y evitar referencias
            // a instancias transientes cuando Hibernate haga flush durante queries.
            if (playerProfile.getPlayerCards() != null) {
                // No reemplazar la colección gestionada por Hibernate cuando orphanRemoval=true.
                // Actualizar la colección en sitio: limpiar y añadir las nuevas entidades.
                if (existing.getPlayerCards() == null) {
                    existing.setPlayerCards(new java.util.ArrayList<>());
                } else {
                    existing.getPlayerCards().clear();
                }
                for (PlayerCard pc : playerProfile.getPlayerCards()) {
                    pc.setPlayerProfile(existing);
                    existing.getPlayerCards().add(pc);
                }
            } else {
                // Si la API no devuelve playerCards, vaciar la colección gestionada en lugar de asignar null
                if (existing.getPlayerCards() != null) {
                    existing.getPlayerCards().clear();
                }
            }
            existing.setCurrentDeck(playerProfile.getCurrentDeck());
            existing.setCurrentDeckSupportCards(playerProfile.getCurrentDeckSupportCards());
            // Reasignar supportCards si vienen en el profile de la API
            if (playerProfile.getSupportCards() != null) {
                for (PlayerCard sc : playerProfile.getSupportCards()) {
                    sc.setPlayerProfile(existing);
                    sc.setSupportCard(true);
                }
                // No hay setter directo para supportCards que almacene la lista separada,
                // pero playerCards contiene tanto normales como support (con supportCard=true)
                // si se desea mantener list separado se puede añadir; aquí actualizamos
                // la colección principal para incluir las support cards si no están ya.
                if (existing.getPlayerCards() == null) {
                    existing.setPlayerCards(new java.util.ArrayList<>());
                }
                for (PlayerCard sc : playerProfile.getSupportCards()) {
                    boolean present = existing.getPlayerCards().stream().anyMatch(c -> Objects.equals(c.getCardId(), sc.getCardId()) && Boolean.TRUE.equals(c.getSupportCard()));
                    if (!present) {
                        sc.setPlayerProfile(existing);
                        sc.setSupportCard(true);
                        existing.getPlayerCards().add(sc);
                    }
                }
            }
            // Mantener la carta favorita aparte para evitar asignar una instancia transiente
            // directamente al entity gestionado por Hibernate.
            PlayerCard favFromApi = playerProfile.getCurrentFavouriteCard();
            existing.setCurrentFavouriteCard(null);
            existing.setCurrentPathOfLegendSeasonResult(playerProfile.getCurrentPathOfLegendSeasonResult());
            existing.setLastPathOfLegendSeasonResult(playerProfile.getLastPathOfLegendSeasonResult());
            existing.setBestPathOfLegendSeasonResult(playerProfile.getBestPathOfLegendSeasonResult());
            existing.setProgress(playerProfile.getProgress());
            // Persistir o asociar mazos antes de guardar el perfil para evitar referencias transientes
            existing.setCurrentDeck(deckService.persistDeckIfNeeded(existing.getCurrentDeck()));
            existing.setCurrentDeckSupportCards(deckService.persistDeckIfNeeded(existing.getCurrentDeckSupportCards()));
            // Guardar el perfil primero sin la carta favorita. Después persistir la carta favorita asociada
            // al perfil ya guardado para evitar referencias transientes.
            savedProfile = playerProfileRepository.save(existing);
            if (favFromApi != null) {
                PlayerCard savedFav = playerCardService.persistFavouriteCardIfNeeded(savedProfile, favFromApi);
                savedProfile.setCurrentFavouriteCard(savedFav);
                savedProfile = playerProfileRepository.save(savedProfile);
            }
        } else {
            // Persistir o asociar mazos antes de guardar el perfil para evitar referencias transientes
            PlayerCard favFromApi = playerProfile.getCurrentFavouriteCard();
            playerProfile.setCurrentFavouriteCard(null);
            playerProfile.setCurrentDeck(deckService.persistDeckIfNeeded(playerProfile.getCurrentDeck()));
            playerProfile.setCurrentDeckSupportCards(deckService.persistDeckIfNeeded(playerProfile.getCurrentDeckSupportCards()));
            // Guardar perfil primero sin la carta favorita
            savedProfile = playerProfileRepository.save(playerProfile);
            // Persistir la carta favorita asociada al perfil guardado
            if (favFromApi != null) {
                PlayerCard savedFav = playerCardService.persistFavouriteCardIfNeeded(savedProfile, favFromApi);
                savedProfile.setCurrentFavouriteCard(savedFav);
                savedProfile = playerProfileRepository.save(savedProfile);
            }
            statsChanged = true; // nuevo perfil -> snapshot
        }
        // Crear snapshot si las estadísticas cambiaron.
        // La importación asíncrona de batallas se encarga de crear snapshot si fue necesario.
        if (statsChanged) {
            snapshotService.saveSnapshot(savedProfile);
        }
        // Guardar/actualizar playerCards siempre (pueden cambiar aunque estadísticas no)
        playerCardService.saveCardsFromProfile(savedProfile);
        return PlayerProfileMapper.toDTO(savedProfile);
    }

    public boolean existsLocallyOrInApi(String playerTag) {
        if (existsByTag(playerTag)) {return true;}
        String responseBody = webClientHelper.fetchGetWithRetries(webClient, "/players/{tag}", playerTag);
        if (responseBody == null || responseBody.isBlank()) return false;

        JsonNode node = objectMapper.readTree(responseBody);
        if (node == null || node.isNull()) return false;
        if (node.has("reason")) return false;
        return node.has("tag") || node.has("name");
    }

    private boolean existsByTag(String tag) {
        return playerProfileRepository.findByTag(tag).isPresent();
    }

    private boolean hasSnapshotRelevantChanges(PlayerProfile existing, PlayerProfile updated) {
        if (existing == null || updated == null) return true;
        if (safeEquals(existing.getTrophies(), updated.getTrophies())) return true;
        if (safeEquals(existing.getBestTrophies(), updated.getBestTrophies())) return true;
        if (safeEquals(existing.getWins(), updated.getWins())) return true;
        if (safeEquals(existing.getLosses(), updated.getLosses())) return true;
        if (safeEquals(existing.getBattleCount(), updated.getBattleCount())) return true;
        if (safeEquals(existing.getThreeCrownWins(), updated.getThreeCrownWins())) return true;
        if (safeEquals(existing.getChallengeCardsWon(), updated.getChallengeCardsWon())) return true;
        if (safeEquals(existing.getChallengeMaxWins(), updated.getChallengeMaxWins())) return true;
        if (safeEquals(existing.getTournamentCardsWon(), updated.getTournamentCardsWon())) return true;
        if (safeEquals(existing.getTournamentBattleCount(), updated.getTournamentBattleCount())) return true;
        if (safeEquals(existing.getDonations(), updated.getDonations())) return true;
        if (safeEquals(existing.getDonationsReceived(), updated.getDonationsReceived())) return true;
        if (safeEquals(existing.getTotalDonations(), updated.getTotalDonations())) return true;
        if (safeEquals(existing.getWarDayWins(), updated.getWarDayWins())) return true;
        if (safeEquals(existing.getClanCardsCollected(), updated.getClanCardsCollected())) return true;
        if (safeEquals(existing.getStarPoints(), updated.getStarPoints())) return true;
        return safeEquals(existing.getExpPoints(), updated.getExpPoints());
    }

    private boolean safeEquals(Integer a, Integer b) {
        if (a == null && b == null) return false;
        if (a == null || b == null) return true;
        return !a.equals(b);
    }


    private PlayerProfile mapApiResponseToEntity(JsonNode json) {
        PlayerProfile profile = new PlayerProfile();
        profile.setTag(readText(json, "tag"));
        profile.setName(readText(json, "name"));
        profile.setExpLevel(readInteger(json, "expLevel"));
        profile.setTrophies(readInteger(json, "trophies"));
        profile.setBestTrophies(readInteger(json, "bestTrophies"));
        profile.setWins(readInteger(json, "wins"));
        profile.setLosses(readInteger(json, "losses"));
        profile.setBattleCount(readInteger(json, "battleCount"));
        profile.setThreeCrownWins(readInteger(json, "threeCrownWins"));
        profile.setChallengeCardsWon(readInteger(json, "challengeCardsWon"));
        profile.setChallengeMaxWins(readInteger(json, "challengeMaxWins"));
        profile.setTournamentCardsWon(readInteger(json, "tournamentCardsWon"));
        profile.setTournamentBattleCount(readInteger(json, "tournamentBattleCount"));
        profile.setRole(readText(json, "role"));
        profile.setDonations(readInteger(json, "donations"));
        profile.setDonationsReceived(readInteger(json, "donationsReceived"));
        profile.setTotalDonations(readInteger(json, "totalDonations"));
        profile.setWarDayWins(readInteger(json, "warDayWins"));
        profile.setClanCardsCollected(readInteger(json, "clanCardsCollected"));
        profile.setStarPoints(readInteger(json, "starPoints"));
        profile.setExpPoints(readInteger(json, "expPoints"));
        profile.setLegacyTrophyRoadHighScore(readInteger(json, "legacyTrophyRoadHighScore"));
        profile.setTotalExpPoints(readInteger(json, "totalExpPoints"));


        profile.setClan(clanService.resolveClanFromNode(json.get("clan")));
        profile.setArena(arenaService.resolveArenaFromNode(json.get("arena")));

        profile.setLeagueStatistics(leagueStadisticService.resolveLeagueStadisticsFromNode(json.get("leagueStatistics")));


        //profile.setLeagueStatistics(readJsonText(json, "leagueStatistics"));
        profile.setBadges(readJsonText(json, "badges"));
        profile.setAchievements(readJsonText(json, "achievements"));

        // Parse cards array into List<PlayerCard>
        JsonNode cardsNode = json.get("cards");
        if (cardsNode != null && cardsNode.isArray()) {
            List<PlayerCard> cardList = new ArrayList<>();
            for (JsonNode c : cardsNode) {
                PlayerCard card = playerCardService.parseCard(c);
                card.setPlayerProfile(profile);
                card.setSupportCard(false);

                cardList.add(card);
            }
            profile.setPlayerCards(cardList);
        } else {
            profile.setPlayerCards(null);
        }

        JsonNode supportNode = json.get("supportCards");
        if (supportNode != null && supportNode.isArray()) {
            List<PlayerCard> supportList = new ArrayList<>();
            for (JsonNode s : supportNode) {
                PlayerCard card = playerCardService.parseCard(s);
                card.setSupportCard(true);
                supportList.add(card);
            }
            profile.setSupportCards(supportList);
        } else {
            profile.setSupportCards(null);
        }

        // currentDeck may be an object or an array of cards (from API). Handle both.
        JsonNode deckNode = json.get("currentDeck");
        if (deckNode != null && !deckNode.isNull()) {
            if (deckNode.isArray()) {
                List<PlayerCard> deckCards = new ArrayList<>();
                for (JsonNode c : deckNode) {
                    PlayerCard card = playerCardService.parseCard(c);
                    deckCards.add(card);
                }
                DeckRequestDTO deckDto = new DeckRequestDTO();
                deckDto.setArchetype(null);
                deckDto.setPlayerCards(deckCards);
                profile.setCurrentDeck(DeckMapper.toEntity(deckDto));
            } else {
                DeckRequestDTO deckDto = objectMapper.convertValue(deckNode, DeckRequestDTO.class);
                profile.setCurrentDeck(DeckMapper.toEntity(deckDto));
            }
        } else {
            profile.setCurrentDeck(null);
        }

        // currentDeckSupportCards may also be an array (support cards) or an object
        JsonNode supportDeckNode = json.get("currentDeckSupportCards");
        if (supportDeckNode != null && !supportDeckNode.isNull()) {
            if (supportDeckNode.isArray()) {
                List<PlayerCard> supportCards = new ArrayList<>();
                for (JsonNode c : supportDeckNode) {
                    PlayerCard card = playerCardService.parseCard(c);
                    supportCards.add(card);
                }
                DeckRequestDTO supportDto = new DeckRequestDTO();
                supportDto.setArchetype(null);
                supportDto.setPlayerCards(supportCards);
                profile.setCurrentDeckSupportCards(DeckMapper.toEntity(supportDto));
            } else {
                DeckRequestDTO supportDto = objectMapper.convertValue(supportDeckNode, DeckRequestDTO.class);
                profile.setCurrentDeckSupportCards(DeckMapper.toEntity(supportDto));
            }
        } else {
            profile.setCurrentDeckSupportCards(null);
        }

        JsonNode favouriteCard = json.get("currentFavouriteCard");
        if (favouriteCard != null && !favouriteCard.isNull()) {
            PlayerCard card = playerCardService.parseCard(favouriteCard);
            profile.setCurrentFavouriteCard(card);
        } else {
            profile.setCurrentFavouriteCard(null);
        }

        profile.setCurrentPathOfLegendSeasonResult(readJsonText(json, "currentPathOfLegendSeasonResult"));
        profile.setLastPathOfLegendSeasonResult(readJsonText(json, "lastPathOfLegendSeasonResult"));
        profile.setBestPathOfLegendSeasonResult(readJsonText(json, "bestPathOfLegendSeasonResult"));
        profile.setProgress(readJsonText(json, "progress"));

        return profile;
    }



    private String readText(JsonNode json, String field) {
        JsonNode node = json.get(field);
        return node == null || node.isNull() ? null : objectMapper.convertValue(node, String.class);
    }

    private Integer readInteger(JsonNode json, String field) {
        JsonNode node = json.get(field);
        return node == null || node.isNull() ? null : node.asInt();
    }

    private String readJsonText(JsonNode json, String field) {
        JsonNode node = json.get(field);
        return node == null || node.isNull() ? null : node.toString();
    }
}

