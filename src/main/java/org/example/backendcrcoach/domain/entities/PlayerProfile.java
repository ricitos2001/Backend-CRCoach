package org.example.backendcrcoach.domain.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "player_profiles")
public class PlayerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false, unique = true)
    private String tag;

    @Column(name = "player_name", nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer expLevel;

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
    private String role;

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
    private Integer legacyTrophyRoadHighScore;

    @Column(nullable = false)
    private Integer totalExpPoints;

    // Objetos y colecciones del JSON original serializados como texto JSON.
    @Lob
    @Column(columnDefinition = "TEXT")
    private String clan;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String arena;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String leagueStatistics;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String badges;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String achievements;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String cards;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String supportCards;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String currentDeck;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String currentDeckSupportCards;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String currentFavouriteCard;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String currentPathOfLegendSeasonResult;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String lastPathOfLegendSeasonResult;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String bestPathOfLegendSeasonResult;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String progress;
}

