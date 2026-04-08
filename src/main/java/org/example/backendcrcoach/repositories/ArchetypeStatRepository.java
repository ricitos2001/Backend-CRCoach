package org.example.backendcrcoach.repositories;

import org.example.backendcrcoach.domain.entities.ArchetypeStat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArchetypeStatRepository extends JpaRepository<ArchetypeStat, Long> {
}

