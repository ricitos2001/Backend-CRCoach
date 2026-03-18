package org.example.backendcrcoach.services;

import jakarta.transaction.Transactional;
import org.example.backendcrcoach.domain.dto.PlayerProfileRequestDTO;
import org.example.backendcrcoach.domain.dto.PlayerProfileResponseDTO;
import org.example.backendcrcoach.domain.entities.PlayerProfile;
import org.example.backendcrcoach.domain.entities.Snapshot;
import org.example.backendcrcoach.mappers.PlayerProfileMapper;
import org.example.backendcrcoach.repositories.PlayerProfileRepository;
import org.example.backendcrcoach.repositories.SnapshotRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.bind.annotation.RequestBody;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.Optional;

@Service
@Transactional
public class PlayerProfileService {

    private final PlayerProfileRepository playerProfileRepository;
    private final SnapshotRepository snapshotRepository;
    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    

    public PlayerProfileService(
            PlayerProfileRepository playerProfileRepository,
            SnapshotRepository snapshotRepository,
            WebClient.Builder builder,
            @Value("${clash.royale.api.url}") String API_URL,
            @Value("${clash.royale.api.key}") String API_KEY

    ) {
        this.playerProfileRepository = playerProfileRepository;
        this.snapshotRepository = snapshotRepository;
        this.webClient = builder
                .baseUrl(API_URL)
                .defaultHeader("Authorization", "Bearer " + API_KEY)
                .build();
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
        Optional.ofNullable(dto.getCards()).ifPresent(profile::setCards);
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

        PlayerProfile savedProfile = playerProfileRepository.findByTag(playerProfile.getTag()).map(existingProfile -> {
                    playerProfile.setId(existingProfile.getId());
                    return playerProfileRepository.save(playerProfile);
                }).orElseGet(() -> playerProfileRepository.save(playerProfile));

        saveSnapshot(savedProfile);
        return PlayerProfileMapper.toDTO(savedProfile);
    }


    // Método para guardar un snapshot de las estadísticas del jugador
    private void saveSnapshot(PlayerProfile profile) {
        Snapshot snapshot = Snapshot.builder()
                .playerProfile(profile)
                .trophies(nonNull(profile.getTrophies()))
                .bestTrophies(nonNull(profile.getBestTrophies()))
                .wins(nonNull(profile.getWins()))
                .losses(nonNull(profile.getLosses()))
                .battleCount(nonNull(profile.getBattleCount()))
                .threeCrownWins(nonNull(profile.getThreeCrownWins()))
                .challengeCardsWon(nonNull(profile.getChallengeCardsWon()))
                .challengeMaxWins(nonNull(profile.getChallengeMaxWins()))
                .tournamentCardsWon(nonNull(profile.getTournamentCardsWon()))
                .tournamentBattleCount(nonNull(profile.getTournamentBattleCount()))
                .donations(nonNull(profile.getDonations()))
                .donationsReceived(nonNull(profile.getDonationsReceived()))
                .totalDonations(nonNull(profile.getTotalDonations()))
                .warDayWins(nonNull(profile.getWarDayWins()))
                .clanCardsCollected(nonNull(profile.getClanCardsCollected()))
                .starPoints(nonNull(profile.getStarPoints()))
                .expPoints(nonNull(profile.getExpPoints()))
                .build();

        snapshotRepository.save(snapshot);
    }

    private Integer nonNull(Integer value) {
        return value == null ? 0 : value;
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
        profile.setCards(readJsonText(json, "cards"));
        profile.setSupportCards(readJsonText(json, "supportCards"));
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

}

