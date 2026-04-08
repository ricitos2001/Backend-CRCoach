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
@Table(name = "archetype_stats")
public class ArchetypeStat {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "weakness_report_id")
    private WeaknessReport weaknessReport;

    @Column(nullable = false)
    private String archetype; // store enum name

    private Long battles;

    private Long wins;

    private Long losses;

    private Double winRate;

    private String label;

    private Instant createdAt;
}

