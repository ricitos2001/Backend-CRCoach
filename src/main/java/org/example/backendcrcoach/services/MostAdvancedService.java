package org.example.backendcrcoach.services;

import jakarta.transaction.Transactional;
import org.example.backendcrcoach.domain.dto.MostAdvancedRequestDTO;
import org.example.backendcrcoach.domain.dto.MostAdvancedResponseDTO;
import org.example.backendcrcoach.domain.entities.MostAdvanced;
import org.example.backendcrcoach.mappers.MostAdvancedMapper;
import org.example.backendcrcoach.repositories.MostAdvancedRepository;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class MostAdvancedService {
    private final MostAdvancedRepository repo;

    public MostAdvancedService(MostAdvancedRepository repo) { this.repo = repo; }

    public MostAdvancedResponseDTO create(MostAdvancedRequestDTO dto) {
        MostAdvanced e = MostAdvancedMapper.toEntity(dto);
        MostAdvanced saved = repo.save(e);
        return MostAdvancedMapper.toDTO(saved);
    }
}

