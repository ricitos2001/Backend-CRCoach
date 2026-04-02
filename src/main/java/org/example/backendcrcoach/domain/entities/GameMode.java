package org.example.backendcrcoach.domain.entities;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entidad que representa el modo de juego (gameMode) presente en los JSON de ejemplo.
 * Contiene el identificador externo (`game_mode_id`) y el nombre.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "game_modes", uniqueConstraints = @UniqueConstraint(columnNames = "game_mode_id"))
public class GameMode {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    // Identificador que viene en el JSON (por ejemplo 72000006)
    @Column(name = "game_mode_id", nullable = false, unique = true)
    private Integer gameModeId;

    @Column(nullable = false)
    private String name;

    @PrePersist
    @PreUpdate
    private void normalize() {
        if (name != null) name = name.trim();
        if (gameModeId == null) gameModeId = 0;
    }
}

