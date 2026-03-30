package org.example.backendcrcoach.domain.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "seasons", uniqueConstraints = @UniqueConstraint(columnNames = "season_id"))
public class Season {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "season_id", nullable = false, unique = true)
    private String seasonId;

    @Column(nullable = false)
    private Integer trophies;

    @Column(nullable = false)
    private Integer bestTrophies;

    @PrePersist
    @PreUpdate
    private void normalize() {
        if (seasonId != null) seasonId = seasonId.trim();
        if (trophies == null) trophies = 0;
        if (bestTrophies == null) bestTrophies = 0;
    }
}

