package org.example.backendcrcoach.analytics;

import org.example.backendcrcoach.domain.entities.PlayerCard;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class ArchetypeClassifier {

    private static final Map<Integer, Archetype> SIGNATURE_CARDS = new HashMap<>();

    // Orden de prioridad explícito para las cartas firmantes
    private static final List<Integer> PRIORITY_CARD_IDS = List.of(
            // Prioridad: arquetipos específicos / wincons
            26000038, // X-Bow
            28000007, // Mortar
            26000055, // Mega Knight
            26000033, // Sparky
            26000067, // Elixir Golem
            26000021, // Hog Rider
            28000015, // Graveyard
            26000003, // Giant
            26000009, // Golem
            26000028, // Lava Hound
            26000004, // PEKKA
            26000056, // Electro Giant
            26000042, // Ram Rider
            26000051, // Ram Rider (alt?)
            26000032, // Miner / Battle Ram
            26000036, // Bandit
            26000062, // Magic Archer / Royal Hogs
            26000059, // Royal Hogs (alt id)
            26000005  // Prince
    );

    static {
        // Mapeo actualizado: asignar IDs a los arquetipos definidos en el enum
        SIGNATURE_CARDS.put(26000038, Archetype.X_BOW); // X-Bow
        SIGNATURE_CARDS.put(28000007, Archetype.MORTAR); // Mortar
        SIGNATURE_CARDS.put(28000015, Archetype.GRAVEYARD); // Graveyard

        SIGNATURE_CARDS.put(26000003, Archetype.GIANT); // Giant
        SIGNATURE_CARDS.put(26000009, Archetype.GOLEM_BEATDOWN); // Golem
        SIGNATURE_CARDS.put(26000028, Archetype.LAVA_HOUND); // Lava Hound
        SIGNATURE_CARDS.put(26000004, Archetype.BEATDOWN); // PEKKA
        SIGNATURE_CARDS.put(26000056, Archetype.BEATDOWN); // Electro Giant

        SIGNATURE_CARDS.put(26000042, Archetype.BRIDGE_SPAM); // Ram Rider
        SIGNATURE_CARDS.put(26000032, Archetype.BRIDGE_SPAM); // Battle Ram
        SIGNATURE_CARDS.put(26000036, Archetype.BRIDGE_SPAM); // Bandit
        SIGNATURE_CARDS.put(26000062, Archetype.BRIDGE_SPAM); // Royal Hogs
        SIGNATURE_CARDS.put(26000005, Archetype.BRIDGE_SPAM); // Prince
        // Más cartas detectadas en response.json -> mapear a arquetipos específicos
        SIGNATURE_CARDS.put(26000055, Archetype.MEGA_KNIGHT); // Mega Knight
        SIGNATURE_CARDS.put(26000033, Archetype.SPARKY); // Sparky
        SIGNATURE_CARDS.put(26000067, Archetype.ELIXIR_GOLEM); // Elixir Golem
        SIGNATURE_CARDS.put(26000021, Archetype.HOG_CYCLE); // Hog Rider -> HOG_CYCLE
        SIGNATURE_CARDS.put(26000059, Archetype.BRIDGE_SPAM); // Royal Hogs (alt id)
        SIGNATURE_CARDS.put(26000032, Archetype.BRIDGE_SPAM); // Miner (note: miner id 26000032 appears in JSON as miner)
        // Goblin Barrel / The Log / Skeletons used for LOGBAIT heuristic (IDs)
        SIGNATURE_CARDS.put(28000004, Archetype.LOGBAIT); // Goblin Barrel
        SIGNATURE_CARDS.put(28000011, Archetype.LOGBAIT); // The Log
        SIGNATURE_CARDS.put(26000010, Archetype.LOGBAIT); // Skeletons
    }

    public Archetype classify(List<PlayerCard> opponentCards) {
        if (opponentCards == null || opponentCards.isEmpty()) return Archetype.CUSTOM;

        // Construir set de IDs presentes en el mazo para comprobaciones por prioridad
        Set<Integer> present = new HashSet<>();
        for (PlayerCard pc : opponentCards) {
            if (pc != null && pc.getCardId() != null) present.add(pc.getCardId());
        }

        // 1. Buscar carta firmante por prioridad (PRIORITY_CARD_IDS)
        // Heurística especial: LOGBAIT si hay Goblin Barrel + The Log + Skeletons
        if (present.contains(28000004) && present.contains(28000011) && present.contains(26000010)) {
            return Archetype.LOGBAIT;
        }

        // Heurística: LAVA_LOOMB (Balloon + Lava Hound)
        if (present.contains(26000006) && present.contains(26000028)) {
            return Archetype.LAVA_LOOMB;
        }

        // Heurística: LUMBER_LOOMB (Balloon + Lumberjack)
        if (present.contains(26000006) && present.contains(26000035)) {
            return Archetype.LUMBER_LOOMB;
        }

        for (Integer id : PRIORITY_CARD_IDS) {
            if (present.contains(id)) {
                Archetype mapped = SIGNATURE_CARDS.get(id);
                if (mapped != null) return mapped;
            }
        }

        // 2. Fallback por elixir medio
        double avgElixir = opponentCards.stream()
                .filter(c -> c != null && c.getElixirCost() != null)
                .mapToInt(PlayerCard::getElixirCost)
                .average()
                .orElse(3.5);

        if (avgElixir <= 3.0) return Archetype.CYCLE;
        if (avgElixir <= 4.0) return Archetype.MIDRANGE;
        return Archetype.CONTROL;
    }
}