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
        // No comprobación por apiId (campo external eliminado). Simplemente crear.
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

    // Búsqueda por apiId eliminada (campo external eliminado)

    public Optional<DeckResponseDTO> update(Long id, DeckRequestDTO dto) {
        return deckRepository.findById(id).map(existing -> {
            if (deckRepository.existsById(id)) {
                throw new IllegalArgumentException("Ya existe un Deck con id: " + id);
            }

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
        // Si el deck ya tiene id (PK), se asume persistido/gestionado
        if (deck.getId() != null) {
            return deckRepository.findById(deck.getId()).orElse(deck);
        }
        // Si no tiene arquetipo calculado, calcularlo ahora usando el classifier
        try {
            if (deck.getArchetype() == null && archetypeClassifier != null) {
                Archetype type = archetypeClassifier.classify(deck.getPlayerCards());
                deck.setArchetype(type);
            }
        } catch (Exception ignored) {
            throw new RuntimeException("Error al clasificar el arquetipo del deck: " + ignored.getMessage(), ignored);
        }

        // Guardar nuevo deck
        return deckRepository.save(deck);
        // No hay apiId ni id: simplemente persistir el deck
    }
}

