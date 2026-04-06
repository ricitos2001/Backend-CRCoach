package org.example.backendcrcoach.domain.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "player_summary_reports")
public class PlayerSummaryReport {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false)
    private String playerTag;

    private Integer trophies;

    private Double winRateLast25;

    private Double winRateLast7d;

    private Integer currentStreak;

    private Long totalBattles;

    private String weakestArchetype;

    private String strongestArchetype;

    private Double avgElixirLastDeck;

    private Instant createdAt;
}

