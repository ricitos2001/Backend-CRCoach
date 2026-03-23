package org.example.backendcrcoach.domain.entities;

import jakarta.persistence.*;
import lombok.*;

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

    // ID del deck en la API externa
    @Column(name = "api_id", unique = true)
    private Long apiId;

    @Column
    private String archetype;

    // Cartas del jugador que componen el deck
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "deck_player_cards",
            joinColumns = @JoinColumn(name = "deck_id"),
            inverseJoinColumns = @JoinColumn(name = "player_card_id")
    )
    private List<PlayerCard> playerCards;
}

