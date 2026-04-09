package org.example.backendcrcoach.services;

import jakarta.transaction.Transactional;
import org.example.backendcrcoach.domain.dto.BattlesRequestDTO;
import org.example.backendcrcoach.domain.dto.BattlesResponseDTO;
import org.example.backendcrcoach.domain.entities.Battles;
import org.example.backendcrcoach.mappers.BattlesMapper;
import org.example.backendcrcoach.repositories.BattlesRepository;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class BattlesService {
    private final BattlesRepository repo;

    public BattlesService(BattlesRepository repo) { this.repo = repo; }

    public BattlesResponseDTO create(BattlesRequestDTO dto) {
        Battles e = BattlesMapper.toEntity(dto);
        Battles saved = repo.save(e);
        return BattlesMapper.toDTO(saved);
    }
}

