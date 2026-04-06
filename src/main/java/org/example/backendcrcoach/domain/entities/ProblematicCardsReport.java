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
@Table(name = "problematic_cards_reports")
public class ProblematicCardsReport {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false)
    private String playerTag;

    private Long totalLosses;

    @Column(columnDefinition = "TEXT")
    private String problematicCardsJson; // JSON serialized list of ProblematicCardDto

    private Instant createdAt;
}

