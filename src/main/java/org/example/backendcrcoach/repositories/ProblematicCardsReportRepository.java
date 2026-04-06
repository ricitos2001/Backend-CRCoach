package org.example.backendcrcoach.repositories;

import org.example.backendcrcoach.domain.entities.ProblematicCardsReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProblematicCardsReportRepository extends JpaRepository<ProblematicCardsReport, Long> {
}

