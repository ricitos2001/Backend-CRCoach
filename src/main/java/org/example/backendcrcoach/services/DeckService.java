package org.example.backendcrcoach.services;

import jakarta.transaction.Transactional;
import org.example.backendcrcoach.analytics.Archetype;
import org.example.backendcrcoach.analytics.ArchetypeClassifier;
import org.example.backendcrcoach.services.CardClassifierService;
import org.example.backendcrcoach.domain.dto.DeckRequestDTO;
import org.example.backendcrcoach.domain.dto.DeckResponseDTO;
import org.example.backendcrcoach.domain.entities.Deck;
import org.example.backendcrcoach.mappers.DeckMapper;
import org.example.backendcrcoach.repositories.DeckRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.example.backendcrcoach.domain.enums.CardUseType;

@Service
@Transactional
public class DeckService {

    private final DeckRepository deckRepository;
    private final ArchetypeClassifier archetypeClassifier;
    private final CardClassifierService cardClassifierService;

    public DeckService(DeckRepository deckRepository, ArchetypeClassifier archetypeClassifier, CardClassifierService cardClassifierService) {
        this.deckRepository = deckRepository;
        this.archetypeClassifier = archetypeClassifier;
        this.cardClassifierService = cardClassifierService;
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

        // Antes de guardar, asegurarse de que cada PlayerCard tenga su useType calculado
        try {
            if (deck.getPlayerCards() != null && cardClassifierService != null) {
                List<org.example.backendcrcoach.domain.entities.PlayerCard> pcs = deck.getPlayerCards();
                for (int i = 0; i < pcs.size(); i++) {
                    org.example.backendcrcoach.domain.entities.PlayerCard pc = pcs.get(i);
                    try {
                        // calcular base si está a null
                        if (pc.getUseType() == null) {
                            pc.setUseType(cardClassifierService.classify(pc));
                        }

                        // Aplicar heurística posicional: primera carta -> EVOLUTION, segunda -> HERO,
                        // tercera -> EVOLUTION si puede, si no HERO; resto -> NORMAL (si no ya establecido)
                        if (i == 0) {
                            if (cardClassifierService.isEvolvable(pc)) {
                                pc.setUseType(CardUseType.EVOLUTION);
                            }
                        } else if (i == 1) {
                            if (cardClassifierService.isHeroCard(pc)) {
                                pc.setUseType(CardUseType.HERO);
                            }
                        } else if (i == 2) {
                            if (cardClassifierService.isEvolvable(pc)) {
                                pc.setUseType(CardUseType.EVOLUTION);
                            } else if (cardClassifierService.isHeroCard(pc)) {
                                pc.setUseType(CardUseType.HERO);
                            }
                        } else {
                            if (pc.getUseType() == null) {
                                pc.setUseType(CardUseType.NORMAL);
                            }
                        }
                    } catch (Exception ignored) {
                        // No bloquear el guardado por un fallo en clasificación de una carta
                    }
                }
            }
        } catch (Exception ignored) {
            // ignorar problemas de clasificación a nivel deck
        }

        // Guardar nuevo deck
        return deckRepository.save(deck);
        // No hay apiId ni id: simplemente persistir el deck
    }
}

