package org.example.backendcrcoach.services;

import jakarta.transaction.Transactional;
import org.example.backendcrcoach.analytics.Archetype;
import org.example.backendcrcoach.analytics.ArchetypeClassifier;
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
    private final ArchetypeClassifier archetypeClassifier;

    public DeckService(DeckRepository deckRepository, ArchetypeClassifier archetypeClassifier) {
        this.deckRepository = deckRepository;
        this.archetypeClassifier = archetypeClassifier;
    }

    public DeckResponseDTO create(DeckRequestDTO dto) {
        Deck deck = DeckMapper.toEntity(dto);
        String fingerprint = Deck.computeFingerprint(deck.getPlayerCards());
        if (fingerprint != null) {
            Optional<Deck> existing = deckRepository.findByFingerprint(fingerprint);
            if (existing.isPresent()) {
                return DeckMapper.toDTO(existing.get());
            }
        }
        deck.setFingerprint(fingerprint);
        Deck saved = deckRepository.save(deck);
        return DeckMapper.toDTO(saved);
    }

    public List<DeckResponseDTO> listAll() {
        return deckRepository.findAll().stream().map(DeckMapper::toDTO).collect(Collectors.toList());
    }

    public Optional<DeckResponseDTO> findById(Long id) {
        return deckRepository.findById(id).map(DeckMapper::toDTO);
    }

    // Búsqueda por apiId eliminada (campo external eliminado)

    public Optional<DeckResponseDTO> update(Long id, DeckRequestDTO dto) {
        return deckRepository.findById(id).map(existing -> {
            Optional.ofNullable(dto.getArchetype()).ifPresent(existing::setArchetype);
            if (dto.getPlayerCards() != null) {
                existing.setPlayerCards(dto.getPlayerCards());
            }
            String fingerprint = Deck.computeFingerprint(existing.getPlayerCards());
            if (fingerprint != null) {
                Optional<Deck> dup = deckRepository.findByFingerprint(fingerprint);
                if (dup.isPresent() && !dup.get().getId().equals(id)) {
                    throw new IllegalArgumentException("Ya existe un Deck con la misma composición de cartas");
                }
            }
            existing.setFingerprint(fingerprint);

            Deck saved = deckRepository.save(existing);
            return DeckMapper.toDTO(saved);
        });
    }

    public void delete(Long id) {
        deckRepository.deleteById(id);
    }

    public Deck persistDeckIfNeeded(Deck deck) {
        if (deck == null) return null;
        if (deck.getId() != null) {
            return deckRepository.findById(deck.getId()).orElse(deck);
        }

        String fingerprint = Deck.computeFingerprint(deck.getPlayerCards());
        if (fingerprint != null) {
            Optional<Deck> existing = deckRepository.findByFingerprint(fingerprint);
            if (existing.isPresent()) {
                return existing.get();
            }
        }
        deck.setFingerprint(fingerprint);

        try {
            if (deck.getArchetype() == null && archetypeClassifier != null) {
                Archetype type = archetypeClassifier.classify(deck.getPlayerCards());
                deck.setArchetype(type);
            }
        } catch (Exception ignored) {
            throw new RuntimeException("Error al clasificar el arquetipo del deck: " + ignored.getMessage(), ignored);
        }

        return deckRepository.save(deck);
    }
}

