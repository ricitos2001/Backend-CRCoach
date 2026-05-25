package org.example.backendcrcoach.domain.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.example.backendcrcoach.analytics.Archetype;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "decks", uniqueConstraints = @UniqueConstraint(columnNames = "fingerprint"))
public class Deck {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(unique = true)
    private String fingerprint;

    @Column
    @Enumerated(EnumType.STRING)
    private Archetype archetype;

    // Cartas del jugador que componen el deck
    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER)
    @JoinColumn(name = "deck_id") // crea la columna deck_id en la tabla player_cards
    private List<PlayerCard> playerCards;

    public static String computeFingerprint(List<PlayerCard> cards) {
        if (cards == null || cards.isEmpty()) return null;
        return cards.stream()
                .sorted(Comparator.comparing(PlayerCard::getCardId))
                .map(c -> c.getCardId() + ":" +
                        (c.getLevel() != null ? c.getLevel() : 0) + ":" +
                        (c.getEvolutionLevel() != null ? c.getEvolutionLevel() : 0))
                .collect(Collectors.joining(","));
    }
}

