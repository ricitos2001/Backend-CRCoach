package org.example.backendcrcoach.services;

import jakarta.transaction.Transactional;
import org.example.backendcrcoach.domain.dto.PlayerEntityRequestDTO;
import org.example.backendcrcoach.domain.dto.PlayerEntityResponseDTO;
import org.example.backendcrcoach.domain.entities.PlayerEntity;
import org.example.backendcrcoach.mappers.DeckMapper;
import org.example.backendcrcoach.mappers.PlayerEntityMapper;
import org.example.backendcrcoach.repositories.ClanRepository;
import org.example.backendcrcoach.repositories.PlayerEntityRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Transactional
public class PlayerEntityService {

    private final PlayerEntityRepository playerEntityRepository;
    private final ClanRepository clanRepository;

    public PlayerEntityService(PlayerEntityRepository playerEntityRepository, ClanRepository clanRepository) {
        this.playerEntityRepository = playerEntityRepository;
        this.clanRepository = clanRepository;
    }

    public Page<PlayerEntityResponseDTO> list(Pageable pageable) {
        return playerEntityRepository.findAll(pageable).map(PlayerEntityMapper::toDTO);
    }

    public PlayerEntityResponseDTO showById(Long id) {
        PlayerEntity entity = playerEntityRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("PlayerEntity no encontrado con id: " + id));
        return PlayerEntityMapper.toDTO(entity);
    }

    public PlayerEntityResponseDTO showByTag(String tag) {
        PlayerEntity entity = playerEntityRepository.findByTag(tag)
                .orElseThrow(() -> new IllegalArgumentException("PlayerEntity no encontrado con tag: " + tag));
        return PlayerEntityMapper.toDTO(entity);
    }

    public PlayerEntityResponseDTO create(PlayerEntityRequestDTO dto) {
        if (dto.getTag() != null && playerEntityRepository.existsByTag(dto.getTag())) {
            throw new IllegalArgumentException("Ya existe un PlayerEntity con tag: " + dto.getTag());
        }

        PlayerEntity entity = PlayerEntityMapper.toEntity(dto);
        if (dto.getClanId() != null) {
            entity.setClan(clanRepository.findById(dto.getClanId())
                    .orElseThrow(() -> new IllegalArgumentException("Clan no encontrado con id: " + dto.getClanId())));
        }

        PlayerEntity saved = playerEntityRepository.save(entity);
        return PlayerEntityMapper.toDTO(saved);
    }

    public PlayerEntityResponseDTO update(Long id, PlayerEntityRequestDTO dto) {
        PlayerEntity entity = playerEntityRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("PlayerEntity no encontrado con id: " + id));

        if (dto.getTag() != null && !dto.getTag().equals(entity.getTag()) && playerEntityRepository.existsByTag(dto.getTag())) {
            throw new IllegalArgumentException("Ya existe un PlayerEntity con tag: " + dto.getTag());
        }

        Optional.ofNullable(dto.getTag()).ifPresent(entity::setTag);
        Optional.ofNullable(dto.getName()).ifPresent(entity::setName);
        Optional.ofNullable(dto.getStartingTrophies()).ifPresent(entity::setStartingTrophies);
        Optional.ofNullable(dto.getCrowns()).ifPresent(entity::setCrowns);
        Optional.ofNullable(dto.getKingTowerHitPoints()).ifPresent(entity::setKingTowerHitPoints);
        Optional.ofNullable(dto.getPrincessTowersHitPoints()).ifPresent(entity::setPrincessTowersHitPoints);
        Optional.ofNullable(dto.getGlobalRank()).ifPresent(entity::setGlobalRank);
        Optional.ofNullable(dto.getElixirLeaked()).ifPresent(entity::setElixirLeaked);

        if (dto.getClanId() != null) {
            entity.setClan(clanRepository.findById(dto.getClanId())
                    .orElseThrow(() -> new IllegalArgumentException("Clan no encontrado con id: " + dto.getClanId())));
        }

        if (dto.getPlayerDeck() != null) {
            if (entity.getPlayerDeck() == null) {
                entity.setPlayerDeck(DeckMapper.toEntity(dto.getPlayerDeck()));
            } else {
                Optional.ofNullable(dto.getPlayerDeck().getApiId()).ifPresent(entity.getPlayerDeck()::setApiId);
                Optional.ofNullable(dto.getPlayerDeck().getArchetype()).ifPresent(entity.getPlayerDeck()::setArchetype);
                Optional.ofNullable(dto.getPlayerDeck().getPlayerCards()).ifPresent(entity.getPlayerDeck()::setPlayerCards);
            }
        }

        PlayerEntity saved = playerEntityRepository.save(entity);
        return PlayerEntityMapper.toDTO(saved);
    }

    public void delete(Long id) {
        if (!playerEntityRepository.existsById(id)) {
            throw new IllegalArgumentException("PlayerEntity no encontrado con id: " + id);
        }
        playerEntityRepository.deleteById(id);
    }
}

