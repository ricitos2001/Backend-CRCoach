package org.example.backendcrcoach.services;

import jakarta.transaction.Transactional;
import org.example.backendcrcoach.domain.dto.ClanRequestDTO;
import org.example.backendcrcoach.domain.dto.ClanResponseDTO;
import org.example.backendcrcoach.domain.entities.Clan;
import org.example.backendcrcoach.mappers.ClanMapper;
import org.example.backendcrcoach.repositories.ClanRepository;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ClanService {

    private final ClanRepository clanRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ClanService(ClanRepository clanRepository) {
        this.clanRepository = clanRepository;
    }

    public ClanResponseDTO createClan(ClanRequestDTO dto) {
        Clan clan = ClanMapper.toEntity(dto);
        Clan saved = clanRepository.save(clan);
        return ClanMapper.toDTO(saved);
    }

    public List<ClanResponseDTO> getAllClans() {
        return clanRepository.findAll().stream().map(ClanMapper::toDTO).collect(Collectors.toList());
    }

    public Optional<ClanResponseDTO> getClanById(Long id) {
        return clanRepository.findById(id).map(ClanMapper::toDTO);
    }

    public Optional<ClanResponseDTO> updateClan(Long id, ClanRequestDTO dto) {
        return clanRepository.findById(id).map(existing -> {
            Clan updated = ClanMapper.toEntity(dto);
            updated.setBadgeId(existing.getBadgeId());
            Clan saved = clanRepository.save(updated);
            return ClanMapper.toDTO(saved);
        });
    }

    public void deleteClan(Long id) {
        clanRepository.deleteById(id);
    }

    public Clan resolveClanFromNode(JsonNode clanNode) {
        if (clanNode == null || clanNode.isNull()) return null;
        String clanTag = readText(clanNode, "tag");
        if (clanTag == null || clanTag.isBlank()) return null;

        Clan clan = clanRepository.findByTag(clanTag).orElseGet(Clan::new);
        clan.setTag(clanTag);
        String clanName = readText(clanNode, "name");
        clan.setName((clanName == null || clanName.isBlank()) ? clanTag : clanName);

        Integer badgeId = readInteger(clanNode, "badgeId");
        if (badgeId != null && clan.getBadgeId() == null) {
            clan.setBadgeId(badgeId.longValue());
        }

        return clanRepository.save(clan);
    }

    private String readText(JsonNode json, String field) {
        JsonNode node = json.get(field);
        return node == null || node.isNull() ? null : objectMapper.convertValue(node, String.class);
    }

    private Integer readInteger(JsonNode json, String field) {
        JsonNode node = json.get(field);
        return node == null || node.isNull() ? null : node.asInt();
    }
}

