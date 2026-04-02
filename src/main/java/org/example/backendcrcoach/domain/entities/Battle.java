package org.example.backendcrcoach.domain.entities;

import jakarta.persistence.*;
import lombok.*;
import org.example.backendcrcoach.domain.entities.GameMode;

/**
 * Entidad para almacenar batallas (Battle) basada en el JSON de ejemplo.
 *
 * Nota: los objetos complejos arena y gameMode se almacenan como texto JSON,
 * mientras que team y opponent se enlazan a PlayerEntity.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "battles")
public class Battle {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    // Campos simples del JSON
    @Column
    private String type;

    // Fecha/hora tal y como viene en el JSON (se puede mapear a Instant/LocalDateTime si se desea)
    @Column
    private String battleTime;

    @Column(name = "is_ladder_tournament")
    private Boolean isLadderTournament;

    @Column
    private String deckSelection;

    @Column(name = "is_hosted_match")
    private Boolean isHostedMatch;

    @Column
    private Integer leagueNumber;

    @ManyToOne
    @JoinColumn(name = "arena_entity_id")
    private Arena arena;

    @ManyToOne
    @JoinColumn(name = "game_mode_id")
    private GameMode gameMode;

    @ManyToOne
    @JoinColumn(name = "team_player_entity_id")
    private PlayerEntity team;

    @ManyToOne
    @JoinColumn(name = "opponent_player_entity_id")
    private PlayerEntity opponent;
}
