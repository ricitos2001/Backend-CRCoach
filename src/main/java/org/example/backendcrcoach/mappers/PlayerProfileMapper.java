package org.example.backendcrcoach.mappers;

import org.example.backendcrcoach.domain.dto.PlayerProfileRequestDTO;
import org.example.backendcrcoach.domain.dto.PlayerProfileResponseDTO;
import org.example.backendcrcoach.domain.entities.PlayerProfile;

public class PlayerProfileMapper {

    public static PlayerProfile toEntity(PlayerProfileRequestDTO dto) {
        PlayerProfile profile = new PlayerProfile();
        profile.setTag(dto.getTag());
        profile.setName(dto.getName());
        profile.setExpLevel(dto.getExpLevel());
        profile.setTrophies(dto.getTrophies());
        profile.setBestTrophies(dto.getBestTrophies());
        profile.setWins(dto.getWins());
        profile.setLosses(dto.getLosses());
        profile.setBattleCount(dto.getBattleCount());
        profile.setThreeCrownWins(dto.getThreeCrownWins());
        profile.setChallengeCardsWon(dto.getChallengeCardsWon());
        profile.setChallengeMaxWins(dto.getChallengeMaxWins());
        profile.setTournamentCardsWon(dto.getTournamentCardsWon());
        profile.setTournamentBattleCount(dto.getTournamentBattleCount());
        profile.setRole(dto.getRole());
        profile.setDonations(dto.getDonations());
        profile.setDonationsReceived(dto.getDonationsReceived());
        profile.setTotalDonations(dto.getTotalDonations());
        profile.setWarDayWins(dto.getWarDayWins());
        profile.setClanCardsCollected(dto.getClanCardsCollected());
        profile.setStarPoints(dto.getStarPoints());
        profile.setExpPoints(dto.getExpPoints());
        profile.setLegacyTrophyRoadHighScore(dto.getLegacyTrophyRoadHighScore());
        profile.setTotalExpPoints(dto.getTotalExpPoints());

        profile.setClan(dto.getClan());
        profile.setArena(dto.getArena());
        profile.setLeagueStatistics(dto.getLeagueStatistics());
        profile.setBadges(dto.getBadges());
        profile.setAchievements(dto.getAchievements());
        profile.setPlayerCards(dto.getPlayerCards());
        profile.setSupportCards(dto.getSupportCards());
        profile.setCurrentDeck(dto.getCurrentDeck());
        profile.setCurrentDeckSupportCards(dto.getCurrentDeckSupportCards());
        profile.setCurrentFavouriteCard(dto.getCurrentFavouriteCard());
        profile.setCurrentPathOfLegendSeasonResult(dto.getCurrentPathOfLegendSeasonResult());
        profile.setLastPathOfLegendSeasonResult(dto.getLastPathOfLegendSeasonResult());
        profile.setBestPathOfLegendSeasonResult(dto.getBestPathOfLegendSeasonResult());
        profile.setProgress(dto.getProgress());

        return profile;
    }

    public static PlayerProfileResponseDTO toDTO(PlayerProfile profile) {
        return new PlayerProfileResponseDTO(
                profile.getId(),
                profile.getTag(),
                profile.getName(),
                profile.getExpLevel(),
                profile.getTrophies(),
                profile.getBestTrophies(),
                profile.getWins(),
                profile.getLosses(),
                profile.getBattleCount(),
                profile.getThreeCrownWins(),
                profile.getChallengeCardsWon(),
                profile.getChallengeMaxWins(),
                profile.getTournamentCardsWon(),
                profile.getTournamentBattleCount(),
                profile.getRole(),
                profile.getDonations(),
                profile.getDonationsReceived(),
                profile.getTotalDonations(),
                profile.getWarDayWins(),
                profile.getClanCardsCollected(),
                profile.getStarPoints(),
                profile.getExpPoints(),
                profile.getLegacyTrophyRoadHighScore(),
                profile.getTotalExpPoints(),
                profile.getClan(),
                profile.getArena(),
                profile.getLeagueStatistics(),
                profile.getBadges(),
                profile.getAchievements(),
                profile.getPlayerCards(),
                profile.getSupportCards(),
                profile.getCurrentDeck(),
                profile.getCurrentDeckSupportCards(),
                profile.getCurrentFavouriteCard(),
                profile.getCurrentPathOfLegendSeasonResult(),
                profile.getLastPathOfLegendSeasonResult(),
                profile.getBestPathOfLegendSeasonResult(),
                profile.getProgress()
        );
    }
}

