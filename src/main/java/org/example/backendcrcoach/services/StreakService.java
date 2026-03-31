package org.example.backendcrcoach.services;

import org.example.backendcrcoach.domain.dto.StreakRequestDTO;
import org.example.backendcrcoach.domain.dto.StreakResponseDTO;
import org.example.backendcrcoach.domain.entities.Streak;
import org.example.backendcrcoach.mappers.StreakMapper;
import org.example.backendcrcoach.repositories.StreakRepository;
import org.springframework.stereotype.Service;

@Service
public class StreakService {
    private final StreakRepository repo;

    public StreakService(StreakRepository repo) {
        this.repo = repo;
    }

    public StreakResponseDTO create(StreakRequestDTO dto) {
        Streak e = StreakMapper.toEntity(dto);
        Streak saved = repo.save(e);
        return StreakMapper.toDTO(saved);
    }
}

