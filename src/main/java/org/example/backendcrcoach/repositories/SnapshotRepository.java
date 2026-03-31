package org.example.backendcrcoach.repositories;
import org.example.backendcrcoach.domain.entities.Snapshot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
public interface SnapshotRepository extends JpaRepository<Snapshot, Long> {
    Page<Snapshot> findByPlayerProfileTagOrderByCapturedAtDesc(String playerTag, Pageable pageable);
    Page<Snapshot> findByPlayerProfileIdOrderByCapturedAtDesc(Long playerProfileId, Pageable pageable);
    Page<Snapshot> findByPlayerProfileTagAndCapturedAtBetweenOrderByCapturedAtDesc(
            String playerTag, 
            LocalDateTime from, 
            LocalDateTime to, 
            Pageable pageable
    );

    // Obtener el último snapshot por tag
    java.util.Optional<Snapshot> findTopByPlayerProfileTagOrderByCapturedAtDesc(String playerTag);

    // Obtener snapshots posteriores a una fecha para calcular delta en 24h
    java.util.List<Snapshot> findByPlayerProfileTagAndCapturedAtAfterOrderByCapturedAtDesc(String playerTag, LocalDateTime after);
    @Modifying
    @Query("DELETE FROM Snapshot s WHERE s.capturedAt < :cutoffDate")
    long deleteByCreatedAtBefore(@Param("cutoffDate") LocalDateTime cutoffDate);
}
