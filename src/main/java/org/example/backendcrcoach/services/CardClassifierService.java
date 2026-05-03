package org.example.backendcrcoach.services;

import org.example.backendcrcoach.domain.entities.Card;
import org.example.backendcrcoach.domain.entities.PlayerCard;
import org.example.backendcrcoach.domain.enums.CardUseType;
import org.example.backendcrcoach.repositories.CardRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CardClassifierService {

    private final CardRepository cardRepository;

    public CardClassifierService(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    public CardUseType classify(PlayerCard p) {
        if (p == null) return CardUseType.NORMAL;

        Integer level = p.getLevel();
        Integer maxLevel = p.getMaxLevel();

        // 1) Evo por nivel (nivel > maxLevel)
        if (level != null && maxLevel != null && level > maxLevel) {
            return CardUseType.EVOLUTION;
        }

        // 2) Evo por icono (si la API proporcionó evolutionMedium en el nodo de la carta)
        if (p.getIconUrl() != null && p.getIconUrl().getEvolutionMedium() != null) {
            return CardUseType.EVOLUTION;
        }

        Integer cardId = p.getCardId();
        if (cardId != null) {
            Optional<Card> cardOpt = cardRepository.findByCardId(cardId);
            if (cardOpt.isPresent()) {
                Card card = cardOpt.get();

                // Hero si el catálogo tiene heroMedium o rarity == "hero"
                if (card.getIconUrl() != null && card.getIconUrl().getHeroMedium() != null) {
                    return CardUseType.HERO;
                }
                if (card.getRarity() != null && "hero".equalsIgnoreCase(card.getRarity())) {
                    return CardUseType.HERO;
                }

                // Fallback: si el catálogo permite evoluciones y level > card.maxLevel
                if (card.getMaxEvolutionLevel() != null && card.getMaxEvolutionLevel() > 0
                        && level != null && card.getMaxLevel() != null && level > card.getMaxLevel()) {
                    return CardUseType.EVOLUTION;
                }
            }
        }

        return CardUseType.NORMAL;
    }
}

