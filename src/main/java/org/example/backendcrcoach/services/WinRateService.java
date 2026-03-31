package org.example.backendcrcoach.services;

import org.example.backendcrcoach.domain.dto.WinRateRequestDTO;
import org.example.backendcrcoach.domain.dto.WinRateResponseDTO;
import org.example.backendcrcoach.domain.entities.WinRate;
import org.example.backendcrcoach.mappers.WinRateMapper;
import org.example.backendcrcoach.repositories.WinRateRepository;
import org.springframework.stereotype.Service;

@Service
public class WinRateService {
    private final WinRateRepository repository;

    public WinRateService(WinRateRepository repository) {
        this.repository = repository;
    }

    public WinRateResponseDTO create(WinRateRequestDTO dto) {
        WinRate entity = WinRateMapper.toEntity(dto);
        WinRate saved = repository.save(entity);
        return WinRateMapper.toDTO(saved);
    }
}

