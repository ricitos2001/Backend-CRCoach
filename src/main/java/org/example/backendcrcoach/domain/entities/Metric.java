package org.example.backendcrcoach.domain.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Entidad que representa la estructura de docs/metric.json
 * Algunos campos son tomados directamente del perfil del usuario (ej: trophies, bestTrophies, arena)
 * Otros son valores calculados a partir de registros (batallas, objetivos, notificaciones, etc.)
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "metrics")
public class Metric {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    // Datos identificativos del perfil (provienen del perfil del usuario)
    @Column(nullable = false)
    private String tag;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private LocalDateTime generatedAt;

    // Valores snapshot provenientes del perfil
    @Column(nullable = false)
    private Integer trophies;

    @Column(nullable = false)
    private Integer bestTrophies;

    // Valor calculado: diferencia de trofeos vs hace 24h (puede ser negativo)
    @Column
    private Integer changeTrophiesIn24h;

    // Referencia a entidad Arena (viene del perfil del usuario)
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "arena_id")
    private Arena arena;

    // Estadísticas de liga agrupadas (viene del perfil)
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "league_stadistic_id")
    private LeagueStadistic leagueStatistics;

    // Valores calculados a partir del registro de batallas
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "winrate_id")
    private WinRate winRate;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "lossrate_id")
    private LossRate lossRate;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "streak_id")
    private Streak streak;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "battles_id")
    private Battles battles;

    // Valores calculados a partir de objetivos y notificaciones
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "active_goals_id")
    private ActiveGoals activeGoals;

    @Column
    private Integer unreadNotifications;

    // Relación opcional directa con PlayerProfile si se desea enlazar (no necesaria para serialización JSON)
    @ManyToOne
    @JoinColumn(name = "player_profile_id")
    private PlayerProfile playerProfile;

    // Las entidades WinRate, Streak, Battles, ActiveGoals y MostAdvanced
    // ahora son entidades separadas en el paquete domain.entities.

}

