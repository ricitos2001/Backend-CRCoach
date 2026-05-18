package org.example.backendcrcoach.services;

import jakarta.transaction.Transactional;
import org.example.backendcrcoach.domain.dto.ClanRequestDTO;
import org.example.backendcrcoach.domain.dto.ClanResponseDTO;
import org.example.backendcrcoach.domain.entities.Clan;
import org.example.backendcrcoach.mappers.ClanMapper;
import org.example.backendcrcoach.repositories.ClanRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
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
        // Para crear un clan explícitamente vía API el cliente debe proporcionar el badgeId
        // (el id real proveniente de la API de Clash Royale). Si no viene, rechazamos
        // la petición para evitar persistir una entidad sin PK (badgeId) ya que no
        // usamos generación automática en la columna badgeId.
        if (dto.getBadgeId() == null) {
            throw new IllegalArgumentException("badgeId es requerido al crear un clan: debe ser el id del badge de la API de Clash Royale");
        }
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
        boolean isNew = clan.getBadgeId() == null;
        clan.setTag(clanTag);
        String clanName = readText(clanNode, "name");
        clan.setName((clanName == null || clanName.isBlank()) ? clanTag : clanName);

        // Intentamos leer el badgeId que proviene de la API externa y asignarlo.
        Long externalBadgeId = readLong(clanNode, "badgeId");
        if (externalBadgeId != null) {
            clan.setBadgeId(externalBadgeId);
        }

        // Si no tenemos un badgeId y la entidad es nueva, no podemos persistir porque
        // el campo `badgeId` es la PK y no se genera automáticamente. Devolveremos
        // null para que el llamador (por ejemplo PlayerProfileService) no asigne
        // un clan transiente sin id. Si ya existía en BD (isNew == false) permitimos
        // actualizar nombre/etiqueta aunque no se haya enviado badgeId.
        if (clan.getBadgeId() == null && isNew) {
            return null;
        }

        try {
            // Ejecutar el INSERT/flush en una transacción nueva para poder capturar
            // correctamente el DataIntegrityViolationException sin marcar la
            // transacción del llamador como rollback-only.
            return saveClanAndFlushInNewTx(clan);
        } catch (DataIntegrityViolationException dive) {
            // Condición de carrera: si otro hilo ya insertó el registro, recuperarlo
            // en una transacción nueva para no utilizar la transacción actual (que
            // puede estar marcada para rollback).
            Long bid = clan.getBadgeId();
            if (bid != null) {
                return findClanByIdOrTagInNewTx(bid, clan.getTag());
            }
            return findClanByTagInNewTx(clan.getTag());
        }
    }

    @org.springframework.transaction.annotation.Transactional(propagation = Propagation.REQUIRES_NEW)
    protected Clan saveClanAndFlushInNewTx(Clan clan) {
        return clanRepository.saveAndFlush(clan);
    }

    @org.springframework.transaction.annotation.Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    protected Clan findClanByIdOrTagInNewTx(Long bid, String tag) {
        return clanRepository.findById(bid).orElseGet(() -> clanRepository.findByTag(tag).orElse(null));
    }

    @org.springframework.transaction.annotation.Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    protected Clan findClanByTagInNewTx(String tag) {
        return clanRepository.findByTag(tag).orElse(null);
    }

    private String readText(JsonNode json, String field) {
        JsonNode node = json.get(field);
        return node == null || node.isNull() ? null : objectMapper.convertValue(node, String.class);
    }

    private Long readLong(JsonNode json, String field) {
        JsonNode node = json.get(field);
        if (node == null || node.isNull()) return null;
        try {
            return node.isNumber() ? node.asLong() : objectMapper.convertValue(node, Long.class);
        } catch (Exception e) {
            return null;
        }
    }
}

