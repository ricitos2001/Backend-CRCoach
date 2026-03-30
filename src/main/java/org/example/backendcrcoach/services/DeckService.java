package org.example.backendcrcoach.services;

import jakarta.transaction.Transactional;
import org.example.backendcrcoach.domain.dto.DeckRequestDTO;
import org.example.backendcrcoach.domain.dto.DeckResponseDTO;
import org.example.backendcrcoach.domain.entities.Deck;
import org.example.backendcrcoach.mappers.DeckMapper;
import org.example.backendcrcoach.repositories.DeckRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class DeckService {

    private final DeckRepository deckRepository;

    public DeckService(DeckRepository deckRepository) {
        this.deckRepository = deckRepository;
    }

    public DeckResponseDTO create(DeckRequestDTO dto) {
        if (dto.getApiId() != null && deckRepository.existsByApiId(dto.getApiId())) {
            throw new IllegalArgumentException("Ya existe un Deck con apiId: " + dto.getApiId());
        }
        Deck deck = DeckMapper.toEntity(dto);
        Deck saved = deckRepository.save(deck);
        return DeckMapper.toDTO(saved);
    }

    public List<DeckResponseDTO> listAll() {
        return deckRepository.findAll().stream().map(DeckMapper::toDTO).collect(Collectors.toList());
    }

    public Optional<DeckResponseDTO> findById(Long id) {
        return deckRepository.findById(id).map(DeckMapper::toDTO);
    }

    public Optional<DeckResponseDTO> findByApiId(Long apiId) {
        return deckRepository.findByApiId(apiId).map(DeckMapper::toDTO);
    }

    public Optional<DeckResponseDTO> update(Long id, DeckRequestDTO dto) {
        return deckRepository.findById(id).map(existing -> {
            if (dto.getApiId() != null && !dto.getApiId().equals(existing.getApiId()) && deckRepository.existsByApiId(dto.getApiId())) {
                throw new IllegalArgumentException("Ya existe un Deck con apiId: " + dto.getApiId());
            }

            Optional.ofNullable(dto.getApiId()).ifPresent(existing::setApiId);
            Optional.ofNullable(dto.getArchetype()).ifPresent(existing::setArchetype);
            if (dto.getPlayerCards() != null) {
                existing.setPlayerCards(dto.getPlayerCards());
            }

            Deck saved = deckRepository.save(existing);
            return DeckMapper.toDTO(saved);
        });
    }

    public void delete(Long id) {
        deckRepository.deleteById(id);
    }

    public Deck persistDeckIfNeeded(Deck deck) {
        if (deck == null) return null;
        if (deck.getId() != null) return deck;
        if (deck.getApiId() != null) {
            return deckRepository.findByApiId(deck.getApiId()).orElseGet(() -> deckRepository.save(deck));
        }
        // No hay apiId ni id: simplemente persistir el deck
        return deckRepository.save(deck);
    }
}

