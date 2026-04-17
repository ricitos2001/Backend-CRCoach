package org.example.backendcrcoach.domain.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.example.backendcrcoach.analytics.Archetype;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "decks")
public class Deck {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column
    @Enumerated(EnumType.STRING)
    private Archetype archetype;

    // Cartas del jugador que componen el deck
    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "deck_id") // crea la columna deck_id en la tabla player_cards
    @JsonIgnore
    private List<PlayerCard> playerCards;
}

