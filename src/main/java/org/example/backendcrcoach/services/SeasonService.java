package org.example.backendcrcoach.services;

import jakarta.transaction.Transactional;
import org.example.backendcrcoach.domain.dto.SeasonRequestDTO;
import org.example.backendcrcoach.domain.dto.SeasonResponseDTO;
import org.example.backendcrcoach.domain.entities.Season;
import org.example.backendcrcoach.mappers.SeasonMapper;
import org.example.backendcrcoach.repositories.SeasonRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class SeasonService {

    private final SeasonRepository seasonRepository;

    public SeasonService(SeasonRepository seasonRepository) {
        this.seasonRepository = seasonRepository;
    }

    public SeasonResponseDTO createSeason(SeasonRequestDTO dto) {
        Season season = SeasonMapper.toEntity(dto);
        Season saved = seasonRepository.save(season);
        return SeasonMapper.toDTO(saved);
    }

    public List<SeasonResponseDTO> getAllSeasons() {
        return seasonRepository.findAll().stream().map(SeasonMapper::toDTO).collect(Collectors.toList());
    }

    public Optional<SeasonResponseDTO> getSeasonById(Long id) {
        return seasonRepository.findById(id).map(SeasonMapper::toDTO);
    }

    public Optional<SeasonResponseDTO> updateSeason(Long id, SeasonRequestDTO dto) {
        return seasonRepository.findById(id).map(existing -> {
            Season updated = SeasonMapper.toEntity(dto);
            updated.setId(existing.getId());
            Season saved = seasonRepository.save(updated);
            return SeasonMapper.toDTO(saved);
        });
    }

    public void deleteSeason(Long id) {
        seasonRepository.deleteById(id);
    }
}

