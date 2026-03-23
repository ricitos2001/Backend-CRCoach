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

    public void saveSnapshot(PlayerProfile profile) {
        SnapshotRequestDTO dto = SnapshotMapper.toRequestDTO(profile);
        Snapshot snapshot = SnapshotMapper.toEntity(dto, profile);
        snapshotRepository.save(snapshot);
    }

    public Page<Snapshot> getSnapshotsByPlayerTag(String playerTag, Pageable pageable) {
        return snapshotRepository.findByPlayerProfileTagOrderByCapturedAtDesc(playerTag, pageable);
    }

    public Page<Snapshot> getSnapshotsByPlayerProfileId(Long playerProfileId, Pageable pageable) {
        return snapshotRepository.findByPlayerProfileIdOrderByCapturedAtDesc(playerProfileId, pageable);
    }

    public Snapshot getSnapshotById(Long id) {
        return snapshotRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Snapshot no encontrada con id: " + id));
    }

    public Page<Snapshot> getSnapshotsByDateRange(String playerTag, LocalDateTime from, LocalDateTime to, Pageable pageable) {
        return snapshotRepository.findByPlayerProfileTagAndCapturedAtBetweenOrderByCapturedAtDesc(playerTag, from, to, pageable);
    }

    public void deleteOldSnapshots(int daysOld) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
        Long deletedCount = snapshotRepository.deleteByCreatedAtBefore(cutoffDate);
        // Podrías loguear aquí si es necesario
    }
}
