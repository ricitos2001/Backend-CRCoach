package org.example.backendcrcoach.repositories;
import org.example.backendcrcoach.domain.entities.Snapshot;
import org.springframework.data.jpa.repository.JpaRepository;
public interface SnapshotRepository extends JpaRepository<Snapshot, Long> {
}
