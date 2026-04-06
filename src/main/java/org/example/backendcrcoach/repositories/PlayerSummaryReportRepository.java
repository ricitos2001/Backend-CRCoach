package org.example.backendcrcoach.repositories;

import org.example.backendcrcoach.domain.entities.PlayerSummaryReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerSummaryReportRepository extends JpaRepository<PlayerSummaryReport, Long> {
}

