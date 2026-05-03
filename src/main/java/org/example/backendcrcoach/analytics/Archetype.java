package org.example.backendcrcoach.analytics;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Archetype {
    // Arquetipos principales
    SIEGE,
    GRAVEYARD,
    BEATDOWN,
    BRIDGE_SPAM,
    CYCLE,
    MIDRANGE,
    CONTROL,

    // Sub-arquetipos / arquetipos específicos / condiciones de victoria
    X_BOW,
    MORTAR,
    GIANT,
    GOLEM_BEATDOWN,
    LAVA_HOUND,
    LAVA_LOOMB,
    LUMBER_LOOMB,
    LOGBAIT,
    MEGA_KNIGHT,
    SPARKY,
    ELIXIR_GOLEM,
    TWO_POINT_SIX,
    HOG_CYCLE,

    // Arquetipos híbridos / especializados
    BAIT,
    SPLIT_LANE,
    HYBRID,

    // Otras / fallback
    CUSTOM,
    UNKNOWN;

    @JsonCreator
    public static Archetype fromString(String key) {
        if (key == null) return UNKNOWN;
        try {
            return Archetype.valueOf(key.toUpperCase());
        } catch (IllegalArgumentException e) {
            // try replace common separators
            String normalized = key.trim().toUpperCase().replace('-', '_').replace(' ', '_');
            try {
                return Archetype.valueOf(normalized);
            } catch (IllegalArgumentException ex) {
                return UNKNOWN;
            }
        }
    }

    @JsonValue
    public String toValue() {
        return this.name();
    }
}

