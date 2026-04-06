package org.example.backendcrcoach.repositories;

import org.example.backendcrcoach.domain.entities.WeaknessReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WeaknessReportRepository extends JpaRepository<WeaknessReport, Long> {
}

