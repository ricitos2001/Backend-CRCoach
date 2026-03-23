package org.example.backendcrcoach.services;

import jakarta.transaction.Transactional;
import org.example.backendcrcoach.domain.dto.PlayerProfileRequestDTO;
import org.example.backendcrcoach.domain.dto.PlayerProfileResponseDTO;
import org.example.backendcrcoach.domain.entities.Card;
import org.example.backendcrcoach.domain.entities.PlayerProfile;
import org.example.backendcrcoach.mappers.PlayerProfileMapper;
import org.example.backendcrcoach.repositories.ArenaRepository;
import org.example.backendcrcoach.repositories.ClanRepository;
import org.example.backendcrcoach.repositories.PlayerProfileRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.bind.annotation.RequestBody;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
// ...existing code...
import org.example.backendcrcoach.domain.entities.PlayerCard;
import org.example.backendcrcoach.domain.entities.IconUrl;

import java.util.ArrayList;
import java.util.List;

import java.util.Optional;

@Service
@Transactional
public class PlayerProfileService {

    private final PlayerProfileRepository playerProfileRepository;
    private final SnapshotService snapshotService;
    private final PlayerCardService playerCardService;
    private final WebClient webClient;
    private final BattleService battleService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ArenaService arenaService;
    private final ClanService clanService;

    public PlayerProfileService(
            PlayerProfileRepository playerProfileRepository,
            SnapshotService snapshotService,
            WebClient.Builder builder,
            @Value("${clash.royale.api.url}") String API_URL,
            @Value("${clash.royale.api.key}") String API_KEY,
            BattleService battleService,
            PlayerCardService playerCardService,
            ArenaService arenaService,
            ClanService clanService) {
        this.playerProfileRepository = playerProfileRepository;
        this.snapshotService = snapshotService;
        this.webClient = builder
                .baseUrl(API_URL)
                .defaultHeader("Authorization", "Bearer " + API_KEY)
                .build();
        this.battleService = battleService;
        this.playerCardService = playerCardService;
        this.arenaService = arenaService;
        this.clanService = clanService;
    }

    public Page<PlayerProfileResponseDTO> list(Pageable pageable) {
        return playerProfileRepository.findAll(pageable).map(PlayerProfileMapper::toDTO);
    }

    public PlayerProfileResponseDTO showById(Long id) {
        PlayerProfile profile = playerProfileRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("PlayerProfile no encontrado con id: " + id));
        return PlayerProfileMapper.toDTO(profile);
    }

    public PlayerProfileResponseDTO showByTag(String playerTag) {
        PlayerProfile profile = playerProfileRepository.findByTag(playerTag).orElseThrow(() -> new IllegalArgumentException("PlayerProfile no encontrado con tag: " + playerTag));
        return PlayerProfileMapper.toDTO(profile);
    }
    public PlayerProfileResponseDTO create(PlayerProfileRequestDTO dto) {
        if (dto.getTag() != null && playerProfileRepository.existsByTag(dto.getTag())) {
            throw new IllegalArgumentException("Ya existe un PlayerProfile con tag: " + dto.getTag());
        }
        PlayerProfile saved = playerProfileRepository.save(PlayerProfileMapper.toEntity(dto));
        return PlayerProfileMapper.toDTO(saved);
    }

    public PlayerProfileResponseDTO update(Long id, @RequestBody PlayerProfileRequestDTO dto) {
        PlayerProfile profile = playerProfileRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("PlayerProfile no encontrado con id: " + id));
        if (dto.getTag() != null && !dto.getTag().equals(profile.getTag()) && playerProfileRepository.existsByTag(dto.getTag())) {
            throw new IllegalArgumentException("Ya existe un PlayerProfile con tag: " + dto.getTag());
        }
        updateBasicFields(dto, profile);
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
        String responseBody = webClient.get()
                .uri("/players/{tag}", "#" + playerTag)
                .retrieve()
                .bodyToMono(String.class)
                .block();
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
        // Primero importar batallas (devuelve cuantas batallas nuevas se añadieron)
        int importedBattles = 0;
        try {
            importedBattles = battleService.importBattlesForPlayer(playerTag);
        } catch (Exception e) {
            // no interrumpir el flujo por fallos en batallas
        }

        PlayerProfile savedProfile;
        PlayerProfile existing = playerProfileRepository.findByTag(playerProfile.getTag()).orElse(null);
        boolean statsChanged = false;
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
            existing.setPlayerCards(playerProfile.getPlayerCards());
            existing.setCurrentDeck(playerProfile.getCurrentDeck());
            existing.setCurrentDeckSupportCards(playerProfile.getCurrentDeckSupportCards());
            existing.setCurrentFavouriteCard(playerProfile.getCurrentFavouriteCard());
            existing.setCurrentPathOfLegendSeasonResult(playerProfile.getCurrentPathOfLegendSeasonResult());
            existing.setLastPathOfLegendSeasonResult(playerProfile.getLastPathOfLegendSeasonResult());
            existing.setBestPathOfLegendSeasonResult(playerProfile.getBestPathOfLegendSeasonResult());
            existing.setProgress(playerProfile.getProgress());

            savedProfile = playerProfileRepository.save(existing);
        } else {
            savedProfile = playerProfileRepository.save(playerProfile);
            statsChanged = true; // nuevo perfil -> snapshot
        }

        // Crear snapshot si las estadísticas cambiaron o si se importaron nuevas batallas
        if (statsChanged || importedBattles > 0) {
            snapshotService.saveSnapshot(savedProfile);
        }

        // Guardar/actualizar playerCards siempre (pueden cambiar aunque estadísticas no)
        playerCardService.saveCardsFromProfile(savedProfile);

        return PlayerProfileMapper.toDTO(savedProfile);
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

        profile.setLeagueStatistics(readJsonText(json, "leagueStatistics"));
        profile.setBadges(readJsonText(json, "badges"));
        profile.setAchievements(readJsonText(json, "achievements"));

        // Parse cards array into List<PlayerCard>
        JsonNode cardsNode = json.get("cards");
        if (cardsNode != null && cardsNode.isArray()) {
            List<PlayerCard> cardList = new ArrayList<>();
            for (JsonNode c : cardsNode) {
                PlayerCard card = parseCards(c);
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
                PlayerCard card = parseCards(s);
                card.setSupportCard(true);
                supportList.add(card);
            }
            profile.setSupportCards(supportList);
        } else {
            profile.setSupportCards(null);
        }
        profile.setCurrentDeck(readJsonText(json, "currentDeck"));
        profile.setCurrentDeckSupportCards(readJsonText(json, "currentDeckSupportCards"));
        profile.setCurrentFavouriteCard(readJsonText(json, "currentFavouriteCard"));
        profile.setCurrentPathOfLegendSeasonResult(readJsonText(json, "currentPathOfLegendSeasonResult"));
        profile.setLastPathOfLegendSeasonResult(readJsonText(json, "lastPathOfLegendSeasonResult"));
        profile.setBestPathOfLegendSeasonResult(readJsonText(json, "bestPathOfLegendSeasonResult"));
        profile.setProgress(readJsonText(json, "progress"));

        return profile;
    }

    private PlayerCard parseCards(JsonNode s) {
        PlayerCard card = new PlayerCard();
        card.setCardId(s.has("id") && !s.get("id").isNull() ? s.get("id").asInt() : null);
        card.setName(s.has("name") && !s.get("name").isNull() ? s.get("name").asText() : null);
        card.setLevel(s.has("level") && !s.get("level").isNull() ? s.get("level").asInt() : null);
        card.setMaxLevel(s.has("maxLevel") && !s.get("maxLevel").isNull() ? s.get("maxLevel").asInt() : null);
        card.setMaxEvolutionLevel(s.has("maxEvolutionLevel") && !s.get("maxEvolutionLevel").isNull() ? s.get("maxEvolutionLevel").asInt() : null);
        card.setRarity(s.has("rarity") && !s.get("rarity").isNull() ? s.get("rarity").asText() : null);
        card.setCount(s.has("count") && !s.get("count").isNull() ? s.get("count").asInt() : null);
        card.setElixirCost(s.has("elixirCost") && !s.get("elixirCost").isNull() ? s.get("elixirCost").asInt() : null);
        JsonNode iconNode2 = s.get("iconUrls");
        if (iconNode2 != null && !iconNode2.isNull()) {
            IconUrl iconUrl2 = new IconUrl();

            iconUrl2.setMedium(iconNode2.has("medium") && !iconNode2.get("medium").isNull() ? iconNode2.get("medium").asText() : null);
            iconUrl2.setEvolutionMedium(iconNode2.has("evolutionMedium") && !iconNode2.get("evolutionMedium").isNull() ? iconNode2.get("evolutionMedium").asText() : null);
            card.setIconUrl(iconUrl2);
        }
        return card;
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

