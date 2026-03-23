package org.example.backendcrcoach.domain.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "arenas")
public class Arena {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String rawName;

    @PrePersist
    @PreUpdate
    private void normalizeNames() {
        if ((name == null || name.isBlank()) && rawName != null && !rawName.isBlank()) {
            name = rawName;
        }
        if ((rawName == null || rawName.isBlank()) && name != null && !name.isBlank()) {
            rawName = name;
        }
    }
}

