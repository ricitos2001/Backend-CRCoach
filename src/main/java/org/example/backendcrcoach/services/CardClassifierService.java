package org.example.backendcrcoach.services;

import org.example.backendcrcoach.domain.entities.Card;
import org.example.backendcrcoach.domain.entities.PlayerCard;
import org.example.backendcrcoach.domain.enums.CardUseType;
import org.example.backendcrcoach.repositories.CardRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CardClassifierService {

    private static final Logger log = LoggerFactory.getLogger(CardClassifierService.class);

    private final CardRepository cardRepository;

    public CardClassifierService(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    /**
     * Clasifica una PlayerCard en NORMAL / EVOLUTION / HERO.
     * Mejoras:
     * - Comprueba primero los campos provistos por la API de la carta del jugador (player-level metadata)
     *   como maxEvolutionLevel y iconUrls.
     * - Si no hay información suficiente, consulta el catálogo (`Card`) y aplica heurísticas adicionales.
     */
    public CardUseType classify(PlayerCard p) {
        if (p == null) return CardUseType.NORMAL;

        Integer level = p.getLevel();
        Integer maxLevel = p.getMaxLevel();
        Integer playerMaxEvolutionLevel = p.getMaxEvolutionLevel();

        // 1) EVOLUTION si la propia entrada del jugador indica posibilidad de evolución y el nivel supera el max
        if (playerMaxEvolutionLevel != null && playerMaxEvolutionLevel > 0 && level != null && maxLevel != null && level > maxLevel) {
            return CardUseType.EVOLUTION;
        }

        // 2) EVOLUTION por icono en la info de la carta (evolutionMedium presente)
        if (p.getIconUrl() != null && p.getIconUrl().getEvolutionMedium() != null) {
            return CardUseType.EVOLUTION;
        }

        Integer cardId = p.getCardId();
        if (cardId != null) {
            Optional<Card> cardOpt = cardRepository.findByCardId(cardId);
            if (cardOpt.isPresent()) {
                Card card = cardOpt.get();

                // HERO si la info del jugador o del catálogo incluye icono de héroe
                if (p.getIconUrl() != null && p.getIconUrl().getHeroMedium() != null) {
                    return CardUseType.HERO;
                }
                if (card.getIconUrl() != null && card.getIconUrl().getHeroMedium() != null) {
                    return CardUseType.HERO;
                }

                // También revisar el campo rarity tanto del PlayerCard como del catálogo
                if (p.getRarity() != null && "hero".equalsIgnoreCase(p.getRarity())) {
                    return CardUseType.HERO;
                }
                if (card.getRarity() != null && "hero".equalsIgnoreCase(card.getRarity())) {
                    return CardUseType.HERO;
                }

                // Fallback EVOLUTION: si el catálogo soporta evolución y el nivel del jugador supera el maxLevel
                if (card.getMaxEvolutionLevel() != null && card.getMaxEvolutionLevel() > 0
                        && level != null && card.getMaxLevel() != null && level > card.getMaxLevel()) {
                    return CardUseType.EVOLUTION;
                }
            } else {
                log.debug("No se encontró card en catálogo para cardId={}", cardId);
            }
        }

        return CardUseType.NORMAL;
    }

    /**
     * Indica si la carta (según PlayerCard y/o catálogo) soporta evolución.
     */
    public boolean isEvolvable(PlayerCard p) {
        if (p == null) return false;
        if (p.getMaxEvolutionLevel() != null && p.getMaxEvolutionLevel() > 0) return true;
        if (p.getIconUrl() != null && p.getIconUrl().getEvolutionMedium() != null) return true;
        Integer cardId = p.getCardId();
        if (cardId != null) {
            Optional<Card> cardOpt = cardRepository.findByCardId(cardId);
            if (cardOpt.isPresent()) {
                Card card = cardOpt.get();
                if (card.getMaxEvolutionLevel() != null && card.getMaxEvolutionLevel() > 0) return true;
            }
        }
        return false;
    }

    /**
     * Indica si la carta (según PlayerCard y/o catálogo) es un héroe.
     */
    public boolean isHeroCard(PlayerCard p) {
        if (p == null) return false;
        if (p.getIconUrl() != null && p.getIconUrl().getHeroMedium() != null) return true;
        if (p.getRarity() != null && "hero".equalsIgnoreCase(p.getRarity())) return true;
        Integer cardId = p.getCardId();
        if (cardId != null) {
            Optional<Card> cardOpt = cardRepository.findByCardId(cardId);
            if (cardOpt.isPresent()) {
                Card card = cardOpt.get();
                if (card.getIconUrl() != null && card.getIconUrl().getHeroMedium() != null) return true;
                if (card.getRarity() != null && "hero".equalsIgnoreCase(card.getRarity())) return true;
            }
        }
        return false;
    }
}

