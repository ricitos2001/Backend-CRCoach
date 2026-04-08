package org.example.backendcrcoach.domain.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "weakness_reports")
public class WeaknessReport {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false)
    private String playerTag;

    private Long totalBattles;

    private String periodFrom;

    private String periodTo;

    @OneToMany(mappedBy = "weaknessReport", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ArchetypeStat> archetypeStats;

    private String weakestArchetype;

    private String strongestArchetype;

    private Instant createdAt;
}

