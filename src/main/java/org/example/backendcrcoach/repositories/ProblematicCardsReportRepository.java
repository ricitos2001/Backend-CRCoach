package org.example.backendcrcoach.repositories;

import org.example.backendcrcoach.domain.entities.ProblematicCardsReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProblematicCardsReportRepository extends JpaRepository<ProblematicCardsReport, Long> {
	Optional<ProblematicCardsReport> findByPlayerTagAndTotalLosses(String playerTag, Long totalLosses);
}

