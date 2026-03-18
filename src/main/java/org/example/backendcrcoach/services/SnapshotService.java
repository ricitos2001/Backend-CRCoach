package org.example.backendcrcoach.services;
import jakarta.transaction.Transactional;
import org.example.backendcrcoach.domain.dto.*;
import org.example.backendcrcoach.domain.entities.PlayerProfile;
import org.example.backendcrcoach.domain.entities.Snapshot;
import org.example.backendcrcoach.mappers.PlayerProfileMapper;
import org.example.backendcrcoach.repositories.SnapshotRepository;
import org.example.backendcrcoach.mappers.SnapshotMapper;
import org.example.backendcrcoach.repositories.PlayerProfileRepository;
import org.example.backendcrcoach.domain.dto.SnapshotRequestDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
@Service
@Transactional
public class SnapshotService {
    private final SnapshotRepository snapshotRepository;
    private final PlayerProfileRepository playerProfileRepository;

    public SnapshotService(SnapshotRepository snapshotRepository, PlayerProfileRepository playerProfileRepository) {
        this.snapshotRepository = snapshotRepository;
        this.playerProfileRepository = playerProfileRepository;
    }
    /**
     * Guarda una instantánea del perfil del jugador con todas sus métricas actuales.
     */
    public void saveSnapshot(PlayerProfile profile) {
        // Construir un DTO temporal a partir del PlayerProfile y delegar la conversión al mapper
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

        Snapshot snapshot = SnapshotMapper.toEntity(dto, profile);
        snapshotRepository.save(snapshot);
    }

    /**
     * Obtiene todas las instantáneas de un perfil de jugador.
     */
    public Page<Snapshot> getSnapshotsByPlayerTag(String playerTag, Pageable pageable) {
        return snapshotRepository.findByPlayerProfileTagOrderByCapturedAtDesc(playerTag, pageable);
    }
    /**
     * Obtiene todas las instantáneas de un perfil de jugador por su ID.
     */
    public Page<Snapshot> getSnapshotsByPlayerProfileId(Long playerProfileId, Pageable pageable) {
        return snapshotRepository.findByPlayerProfileIdOrderByCapturedAtDesc(playerProfileId, pageable);
    }
    /**
     * Obtiene una instantánea específica por su ID.
     */
    public Snapshot getSnapshotById(Long id) {
        return snapshotRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Snapshot no encontrada con id: " + id));
    }
    /**
     * Obtiene las instantáneas capturadas dentro de un rango de fechas.
     */
    public Page<Snapshot> getSnapshotsByDateRange(String playerTag, LocalDateTime from, LocalDateTime to, Pageable pageable) {
        return snapshotRepository.findByPlayerProfileTagAndCapturedAtBetweenOrderByCapturedAtDesc(playerTag, from, to, pageable);
    }
    /**
     * Elimina instantáneas antiguas (más de X días) para mantener la BD limpia.
     */
    public void deleteOldSnapshots(int daysOld) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
        long deletedCount = snapshotRepository.deleteByCreatedAtBefore(cutoffDate);
        // Podrías loguear aquí si es necesario
    }
}
