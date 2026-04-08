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
@Table(name = "problematic_cards_reports")
public class ProblematicCardsReport {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false)
    private String playerTag;

    private Long totalLosses;

    @OneToMany(mappedBy = "problematicCardsReport", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProblematicCard> problematicCards;

    private Instant createdAt;
}

