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

    @Column(columnDefinition = "TEXT")
    private String byArchetypeJson; // JSON serialized list of ArchetypeStatDto

    private String weakestArchetype;

    private String strongestArchetype;

    private Instant createdAt;
}

