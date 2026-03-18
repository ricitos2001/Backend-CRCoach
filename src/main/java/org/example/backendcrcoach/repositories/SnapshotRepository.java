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
    @Modifying
    @Query("DELETE FROM Snapshot s WHERE s.capturedAt < :cutoffDate")
    long deleteByCreatedAtBefore(@Param("cutoffDate") LocalDateTime cutoffDate);
}
