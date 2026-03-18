package org.example.backendcrcoach.domain.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "player_decks")
public class PlayerDeck {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    // Nombre opcional para el mazo (ej: "Deck 1", "Deck Arena")
    @Column
    private String name;

    // Un mazo contiene exactamente 8 cartas del jugador
    @ManyToMany
    @JoinTable(
            name = "player_deck_cards",
            joinColumns = @JoinColumn(name = "player_deck_id"),
            inverseJoinColumns = @JoinColumn(name = "player_card_id")
    )
    @Size(min = 8, max = 8, message = "Un mazo debe contener exactamente 8 cartas")
    private List<PlayerCard> playerCards = new ArrayList<>();

    // Relación con el perfil del jugador que posee este mazo (opcional)
    @ManyToOne
    @JoinColumn(name = "player_profile_id", nullable = true)
    private PlayerProfile playerProfile;
}

