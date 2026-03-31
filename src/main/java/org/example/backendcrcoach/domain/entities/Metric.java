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
    @Embedded
    private WinRate winRate;

    @Embedded
    private Streak streak;

    @Embedded
    private Battles battles;

    // Valores calculados a partir de objetivos y notificaciones
    @Embedded
    private ActiveGoals activeGoals;

    @Column
    private Integer unreadNotifications;

    // Relación opcional directa con PlayerProfile si se desea enlazar (no necesaria para serialización JSON)
    @ManyToOne
    @JoinColumn(name = "player_profile_id")
    private PlayerProfile playerProfile;

    // --- Embeddables ---
    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WinRate {
        @Column(name = "winrate_last25Battles")
        private Double last25Battles;

        @Column(name = "winrate_last7Days")
        private Double last7Days;
    }

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Streak {
        @Column(name = "streak_current")
        private Integer current;

        // "WIN" | "LOSS" | null
        @Column(name = "streak_type")
        private String type;
    }

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Battles {
        @Column(name = "battles_total")
        private Integer total;

        @Column(name = "battles_last24h")
        private Integer last24h;
    }

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MostAdvanced {
        @Column(name = "mostadvanced_id")
        private Long id;

        @Column(name = "mostadvanced_title", columnDefinition = "TEXT")
        private String title;

        @Column(name = "mostadvanced_progress_percent")
        private Double progressPercent;
    }

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ActiveGoals {
        // count of active goals (calculado a partir de la tabla goals)
        @Column(name = "active_goals_count")
        private Integer count;

        // nearest deadline string (ISO) - calculado
        @Column(name = "active_goals_nearest_deadline")
        private String nearestDeadline;

        @Embedded
        private MostAdvanced mostAdvanced;
    }

}

