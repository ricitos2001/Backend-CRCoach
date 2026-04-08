package org.example.backendcrcoach.repositories;

import org.example.backendcrcoach.domain.entities.ProblematicCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProblematicCardRepository extends JpaRepository<ProblematicCard, Long> {
}

