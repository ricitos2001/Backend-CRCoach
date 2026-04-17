package org.example.backendcrcoach.domain.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "player_entities")
public class PlayerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false)
    private String tag;

    @Column(nullable = false)
    private String name;

    @Column
    private Integer startingTrophies;

    @Column
    private Integer trophyChange;

    @Column
    private Integer crowns;

    @Column
    private Integer kingTowerHitPoints;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "player_entity_princess_towers_hit_points", joinColumns = @JoinColumn(name = "player_entity_id"))
    @Column(name = "hit_points")
    @JsonIgnore
    private List<Integer> princessTowersHitPoints;

    @ManyToOne
    @JoinColumn(name = "clan_entity_id")
    private Clan clan;

    @Column
    private Integer globalRank;

    @Column
    private Double elixirLeaked;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "deck_id")
    private Deck playerDeck;
}

