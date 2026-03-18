package org.example.backendcrcoach.services;

import jakarta.transaction.Transactional;
import org.example.backendcrcoach.domain.dto.PlayerProfileRequestDTO;
import org.example.backendcrcoach.domain.dto.PlayerProfileResponseDTO;
import org.example.backendcrcoach.domain.entities.PlayerProfile;
import org.example.backendcrcoach.domain.entities.User;
import org.example.backendcrcoach.mappers.PlayerProfileMapper;
import org.example.backendcrcoach.repositories.PlayerProfileRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.bind.annotation.RequestBody;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
// ...existing code...
import org.example.backendcrcoach.domain.entities.PlayerCard;
import org.example.backendcrcoach.domain.entities.IconUrl;
import java.util.ArrayList;
import java.util.List;
import org.example.backendcrcoach.services.PlayerCardService;

import java.util.Optional;

@Service
@Transactional
public class PlayerProfileService {

    private final PlayerProfileRepository playerProfileRepository;
    private final SnapshotService snapshotService;
    private final PlayerCardService playerCardService;
    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final BattleService battleService;
    private final UserService userService;

    public PlayerProfileService(
            PlayerProfileRepository playerProfileRepository,
            SnapshotService snapshotService,
            WebClient.Builder builder,
            @Value("${clash.royale.api.url}") String API_URL,
            @Value("${clash.royale.api.key}") String API_KEY,
            BattleService battleService,
            PlayerCardService playerCardService, @Lazy UserService userService) {
        this.playerProfileRepository = playerProfileRepository;
        this.snapshotService = snapshotService;
        this.webClient = builder
                .baseUrl(API_URL)
                .defaultHeader("Authorization", "Bearer " + API_KEY)
                .build();
        this.battleService = battleService;
        this.playerCardService = playerCardService;
        this.userService = userService;
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

    public PlayerProfileResponseDTO getPlayerAndBindToEmail(String playerTag, String email) {
        PlayerProfile savedProfile = fetchAndSavePlayerProfile(playerTag);
        if (email != null && !email.isBlank()) {
            userService.bindPlayerTagToUserByEmail(email, savedProfile.getTag());

            try {
            } catch (RuntimeException e) {
                System.err.println("Error al vincular playerTag al email: " + e.getMessage());
            }
        }
        return PlayerProfileMapper.toDTO(savedProfile);
    }

    private PlayerProfile fetchAndSavePlayerProfile(String playerTag) {
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

        PlayerProfile savedProfile = playerProfileRepository.findByTag(playerProfile.getTag()).map(existingProfile -> {
                    playerProfile.setId(existingProfile.getId());
                    return playerProfileRepository.save(playerProfile);
                }).orElseGet(() -> playerProfileRepository.save(playerProfile));

        snapshotService.saveSnapshot(savedProfile);
        // Guardar las playerCards importadas/parseadas en el perfil (comportamiento similar a saveSnapshot)
        playerCardService.saveCardsFromProfile(savedProfile);
        battleService.importBattlesForPlayer(playerTag);
        return savedProfile;
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

        profile.setClan(readJsonText(json, "clan"));
        profile.setArena(readJsonText(json, "arena"));
        profile.setLeagueStatistics(readJsonText(json, "leagueStatistics"));
        profile.setBadges(readJsonText(json, "badges"));
        profile.setAchievements(readJsonText(json, "achievements"));

        // Parse cards array into List<PlayerCard>
        JsonNode cardsNode = json.get("cards");
        if (cardsNode != null && cardsNode.isArray()) {
            List<PlayerCard> cardList = new ArrayList<>();
            for (JsonNode c : cardsNode) {
                PlayerCard card = new PlayerCard();
                card.setCardId(c.has("id") && !c.get("id").isNull() ? c.get("id").asInt() : null);
                card.setName(c.has("name") && !c.get("name").isNull() ? c.get("name").asText() : null);
                card.setLevel(c.has("level") && !c.get("level").isNull() ? c.get("level").asInt() : null);
                card.setMaxLevel(c.has("maxLevel") && !c.get("maxLevel").isNull() ? c.get("maxLevel").asInt() : null);
                card.setMaxEvolutionLevel(c.has("maxEvolutionLevel") && !c.get("maxEvolutionLevel").isNull() ? c.get("maxEvolutionLevel").asInt() : null);
                card.setRarity(c.has("rarity") && !c.get("rarity").isNull() ? c.get("rarity").asText() : null);
                card.setCount(c.has("count") && !c.get("count").isNull() ? c.get("count").asInt() : null);
                card.setElixirCost(c.has("elixirCost") && !c.get("elixirCost").isNull() ? c.get("elixirCost").asInt() : null);

                // iconUrls object
                JsonNode iconNode = c.get("iconUrls");
                if (iconNode != null && !iconNode.isNull()) {
                    IconUrl iconUrl = new IconUrl();
                    iconUrl.setMedium(iconNode.has("medium") && !iconNode.get("medium").isNull() ? iconNode.get("medium").asText() : null);
                    iconUrl.setEvolutionMedium(iconNode.has("evolutionMedium") && !iconNode.get("evolutionMedium").isNull() ? iconNode.get("evolutionMedium").asText() : null);
                    card.setIconUrl(iconUrl);
                }

                // Marcar relación y flag
                card.setPlayerProfile(profile);
                card.setSupportCard(false);

                cardList.add(card);
            }
            profile.setPlayerCards(cardList);
        } else {
            profile.setPlayerCards(null);
        }
        // Parse supportCards array into List<PlayerCard> with supportCard=true
        JsonNode supportNode = json.get("supportCards");
        if (supportNode != null && supportNode.isArray()) {
            List<PlayerCard> supportList = new ArrayList<>();
            for (JsonNode s : supportNode) {
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

    //Para probar si se obtienen datos de la API y se guardan correctamente en la base de datos, sin necesidad de vincular el playerTag a un email
    public PlayerProfileResponseDTO getPlayer(String playerTag) {
        PlayerProfile savedProfile = fetchAndSavePlayerProfile(playerTag);
        return PlayerProfileMapper.toDTO(savedProfile);
    }

}

