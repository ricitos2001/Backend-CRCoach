package org.example.backendcrcoach.domain.entities;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "player_cards")
public class PlayerCard {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    // ID del card en la API de Clash Royale
    @Column(name = "card_id")
    private Integer cardId;

    @Column
    private String name;

    @Column
    private Integer level;

    @Column
    private Integer maxLevel;

    @Column
    private Integer maxEvolutionLevel;

    @Column
    private String rarity;

    @Column
    private Integer count;

    @Column
    private Integer elixirCost;

    // Relación OneToOne a IconUrl que almacena las URLs de los iconos
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "icon_url_id")
    private IconUrl iconUrl;

    @ManyToOne
    @JoinColumn(name = "player_profile_id", nullable = true)
    @JsonBackReference
    private PlayerProfile playerProfile;

    @Column(name = "support_card")
    private Boolean supportCard;
}
