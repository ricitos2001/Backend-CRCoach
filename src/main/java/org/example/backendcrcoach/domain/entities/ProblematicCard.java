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
@Table(name = "problematic_cards")
public class ProblematicCard {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problematic_cards_report_id")
    private ProblematicCardsReport problematicCardsReport;

    private Integer cardId;

    private String name;

    private Long appearances;

    private Double playerLossRate;

    private String iconUrl;

    private Instant createdAt;
}

