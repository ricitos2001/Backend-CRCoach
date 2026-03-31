package org.example.backendcrcoach.services;

import org.example.backendcrcoach.domain.dto.ActiveGoalsRequestDTO;
import org.example.backendcrcoach.domain.dto.ActiveGoalsResponseDTO;
import org.example.backendcrcoach.domain.entities.ActiveGoals;
import org.example.backendcrcoach.mappers.ActiveGoalsMapper;
import org.example.backendcrcoach.repositories.ActiveGoalsRepository;
import org.springframework.stereotype.Service;

@Service
public class ActiveGoalsService {
    private final ActiveGoalsRepository repo;

    public ActiveGoalsService(ActiveGoalsRepository repo) { this.repo = repo; }

    public ActiveGoalsResponseDTO create(ActiveGoalsRequestDTO dto) {
        ActiveGoals e = ActiveGoalsMapper.toEntity(dto);
        ActiveGoals saved = repo.save(e);
        return ActiveGoalsMapper.toDTO(saved);
    }
}

