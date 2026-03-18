package org.example.backendcrcoach.domain.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "snapshots")
public class Snapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_tag", referencedColumnName = "tag", nullable = false)
    private PlayerProfile playerProfile;

    @Column(nullable = false)
    private Integer trophies;

    @Column(nullable = false)
    private Integer bestTrophies;

    @Column(nullable = false)
    private Integer wins;

    @Column(nullable = false)
    private Integer losses;

    @Column(nullable = false)
    private Integer battleCount;

    @Column(nullable = false)
    private Integer threeCrownWins;

    @Column(nullable = false)
    private Integer challengeCardsWon;

    @Column(nullable = false)
    private Integer challengeMaxWins;

    @Column(nullable = false)
    private Integer tournamentCardsWon;

    @Column(nullable = false)
    private Integer tournamentBattleCount;

    @Column(nullable = false)
    private Integer donations;

    @Column(nullable = false)
    private Integer donationsReceived;

    @Column(nullable = false)
    private Integer totalDonations;

    @Column(nullable = false)
    private Integer warDayWins;

    @Column(nullable = false)
    private Integer clanCardsCollected;

    @Column(nullable = false)
    private Integer starPoints;

    @Column(nullable = false)
    private Integer expPoints;

    @Column(nullable = false)
    private LocalDateTime capturedAt;

    @PrePersist
    public void prePersist() {
        if (capturedAt == null) {
            capturedAt = LocalDateTime.now();
        }
    }
}

