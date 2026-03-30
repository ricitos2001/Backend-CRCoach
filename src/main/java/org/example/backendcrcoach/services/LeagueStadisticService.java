package org.example.backendcrcoach.services;

import jakarta.transaction.Transactional;
import org.example.backendcrcoach.domain.dto.LeagueStadisticRequestDTO;
import org.example.backendcrcoach.domain.dto.LeagueStadisticResponseDTO;
import org.example.backendcrcoach.domain.entities.LeagueStadistic;
import org.example.backendcrcoach.domain.entities.Season;
import org.example.backendcrcoach.domain.dto.SeasonRequestDTO;
import org.example.backendcrcoach.mappers.LeagueStadisticMapper;
import org.example.backendcrcoach.mappers.SeasonMapper;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
// ...existing code...
import org.example.backendcrcoach.repositories.LeagueStadisticRepository;
import org.example.backendcrcoach.repositories.SeasonRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class LeagueStadisticService {

    private final LeagueStadisticRepository repository;
    private final SeasonRepository seasonRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public LeagueStadisticService(LeagueStadisticRepository repository, SeasonRepository seasonRepository) {
        this.repository = repository;
        this.seasonRepository = seasonRepository;
    }

    /**
     * Convierte un JsonNode con la estructura de leagueStatistics en una entidad LeagueStadistic
     * resolviendo y persistiendo las temporadas anidadas cuando sea necesario.
     */
    public LeagueStadistic resolveLeagueStadisticsFromNode(JsonNode node) {
        if (node == null || node.isNull()) return null;

        JsonNode currentNode = node.get("currentSeason");
        JsonNode previousNode = node.get("previousSeason");
        JsonNode bestNode = node.get("bestSeason");

        Season current = null;
        Season previous = null;
        Season best = null;

        try {
            if (currentNode != null && !currentNode.isNull()) {
                SeasonRequestDTO dto = objectMapper.convertValue(currentNode, SeasonRequestDTO.class);
                // Si no viene seasonId para currentSeason (API externa puede omitirlo),
                // generar un seasonId temporal para poder persistir el registro cuando tengamos trophies.
                if ((dto.getSeasonId() == null || dto.getSeasonId().isBlank()) && dto.getTrophies() != null) {
                    dto.setSeasonId("current-" + System.currentTimeMillis());
                }
                current = SeasonMapper.toEntity(dto);
                current = resolveSeason(current);
            }
            if (previousNode != null && !previousNode.isNull()) {
                SeasonRequestDTO dto = objectMapper.convertValue(previousNode, SeasonRequestDTO.class);
                previous = SeasonMapper.toEntity(dto);
                previous = resolveSeason(previous);
            }
            if (bestNode != null && !bestNode.isNull()) {
                SeasonRequestDTO dto = objectMapper.convertValue(bestNode, SeasonRequestDTO.class);
                best = SeasonMapper.toEntity(dto);
                best = resolveSeason(best);
            }
        } catch (RuntimeException e) {
            // si no podemos parsear las temporadas, devolvemos null para evitar romper el flujo
            return null;
        }

        LeagueStadistic ls = new LeagueStadistic();
        ls.setCurrentSeason(current);
        ls.setPreviousSeason(previous);
        ls.setBestSeason(best);
        return repository.save(ls);
    }

    public LeagueStadisticResponseDTO create(LeagueStadisticRequestDTO dto) {
        LeagueStadistic entity = LeagueStadisticMapper.toEntity(dto);

        // ensure seasons are persisted or linked by seasonId
        entity.setCurrentSeason(resolveSeason(entity.getCurrentSeason()));
        entity.setPreviousSeason(resolveSeason(entity.getPreviousSeason()));
        entity.setBestSeason(resolveSeason(entity.getBestSeason()));

        LeagueStadistic saved = repository.save(entity);
        return LeagueStadisticMapper.toDTO(saved);
    }

    public List<LeagueStadisticResponseDTO> list() {
        return repository.findAll().stream().map(LeagueStadisticMapper::toDTO).collect(Collectors.toList());
    }

    public Optional<LeagueStadisticResponseDTO> getById(Long id) {
        return repository.findById(id).map(LeagueStadisticMapper::toDTO);
    }

    public Optional<LeagueStadisticResponseDTO> update(Long id, LeagueStadisticRequestDTO dto) {
        return repository.findById(id).map(existing -> {
            LeagueStadistic updated = LeagueStadisticMapper.toEntity(dto);
            updated.setId(existing.getId());
            updated.setCurrentSeason(resolveSeason(updated.getCurrentSeason()));
            updated.setPreviousSeason(resolveSeason(updated.getPreviousSeason()));
            updated.setBestSeason(resolveSeason(updated.getBestSeason()));
            LeagueStadistic saved = repository.save(updated);
            return LeagueStadisticMapper.toDTO(saved);
        });
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    private Season resolveSeason(Season season) {
        if (season == null) return null;
        String seasonId = season.getSeasonId();
        if (seasonId == null || seasonId.isBlank()) return null;
        return seasonRepository.findBySeasonId(seasonId).orElseGet(() -> seasonRepository.save(season));
    }
}


