package org.example.backendcrcoach.mappers;

import org.example.backendcrcoach.domain.dto.SnapshotRequestDTO;
import org.example.backendcrcoach.domain.dto.SnapshotResponseDTO;
import org.example.backendcrcoach.domain.entities.PlayerProfile;
import org.example.backendcrcoach.domain.entities.Snapshot;
public class SnapshotMapper {
    public static Snapshot toEntity(SnapshotRequestDTO dto, PlayerProfile profile) {
        Snapshot snapshot = new Snapshot();
        snapshot.setPlayerProfile(profile);
        snapshot.setTrophies(dto.getTrophies());
        snapshot.setBestTrophies(dto.getBestTrophies());
        snapshot.setWins(dto.getWins());
        snapshot.setLosses(dto.getLosses());
        snapshot.setBattleCount(dto.getBattleCount());
        snapshot.setThreeCrownWins(dto.getThreeCrownWins());
        snapshot.setChallengeCardsWon(dto.getChallengeCardsWon());
        snapshot.setChallengeMaxWins(dto.getChallengeMaxWins());
        snapshot.setTournamentCardsWon(dto.getTournamentCardsWon());
        snapshot.setTournamentBattleCount(dto.getTournamentBattleCount());
        snapshot.setDonations(dto.getDonations());
        snapshot.setDonationsReceived(dto.getDonationsReceived());
        snapshot.setTotalDonations(dto.getTotalDonations());
        snapshot.setWarDayWins(dto.getWarDayWins());
        snapshot.setClanCardsCollected(dto.getClanCardsCollected());
        snapshot.setStarPoints(dto.getStarPoints());
        snapshot.setExpPoints(dto.getExpPoints());
        return snapshot;
    }
    public static SnapshotResponseDTO toDTO(Snapshot snapshot) {
        return new SnapshotResponseDTO(
                snapshot.getId(),
                snapshot.getPlayerProfile() != null ? snapshot.getPlayerProfile().getTag() : null,
                snapshot.getTrophies(),
                snapshot.getBestTrophies(),
                snapshot.getWins(),
                snapshot.getLosses(),
                snapshot.getBattleCount(),
                snapshot.getThreeCrownWins(),
                snapshot.getChallengeCardsWon(),
                snapshot.getChallengeMaxWins(),
                snapshot.getTournamentCardsWon(),
                snapshot.getTournamentBattleCount(),
                snapshot.getDonations(),
                snapshot.getDonationsReceived(),
                snapshot.getTotalDonations(),
                snapshot.getWarDayWins(),
                snapshot.getClanCardsCollected(),
                snapshot.getStarPoints(),
                snapshot.getExpPoints(),
                snapshot.getCapturedAt()
        );
    }

    public static SnapshotRequestDTO toRequestDTO(PlayerProfile profile) {
        SnapshotRequestDTO dto = new SnapshotRequestDTO();
        dto.setTrophies(profile.getTrophies());
        dto.setBestTrophies(profile.getBestTrophies());
        dto.setWins(profile.getWins());
        dto.setLosses(profile.getLosses());
        dto.setBattleCount(profile.getBattleCount());
        dto.setThreeCrownWins(profile.getThreeCrownWins());
        dto.setChallengeCardsWon(profile.getChallengeCardsWon());
        dto.setChallengeMaxWins(profile.getChallengeMaxWins());
        dto.setTournamentCardsWon(profile.getTournamentCardsWon());
        dto.setTournamentBattleCount(profile.getTournamentBattleCount());
        dto.setDonations(profile.getDonations());
        dto.setDonationsReceived(profile.getDonationsReceived());
        dto.setTotalDonations(profile.getTotalDonations());
        dto.setWarDayWins(profile.getWarDayWins());
        dto.setClanCardsCollected(profile.getClanCardsCollected());
        dto.setStarPoints(profile.getStarPoints());
        dto.setExpPoints(profile.getExpPoints());
        return dto;
    }
}
