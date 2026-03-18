package org.example.backendcrcoach.domain.entities;

import jakarta.persistence.*;
import lombok.*;
import org.example.backendcrcoach.domain.entities.IconUrl;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "cards")
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    // ID del card en la API de Clash Royale
    @Column(name = "card_id", unique = true)
    private Integer cardId;

    @Column
    private String name;

    @Column
    private Integer maxLevel;

    @Column
    private Integer maxEvolutionLevel;

    @Column
    private String rarity;

    @Column
    private Integer elixirCost;

    // Relación OneToOne a IconUrl que almacena las URLs de los iconos
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "icon_url_id")
    private IconUrl iconUrl;

    @ManyToOne
    @JoinColumn(name = "player_profile_id", nullable = true)
    private PlayerProfile playerProfile;

}

