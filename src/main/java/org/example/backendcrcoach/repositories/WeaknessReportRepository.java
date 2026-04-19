package org.example.backendcrcoach.repositories;

import org.example.backendcrcoach.domain.entities.WeaknessReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WeaknessReportRepository extends JpaRepository<WeaknessReport, Long> {
	Optional<WeaknessReport> findByPlayerTagAndPeriodFromAndPeriodTo(String playerTag, String periodFrom, String periodTo);
}

