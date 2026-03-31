package org.example.backendcrcoach.domain.entities;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import org.example.backendcrcoach.domain.entities.LeagueStadistic;
import com.fasterxml.jackson.annotation.JsonManagedReference;

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

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer expLevel;

    //SNAPSHOT DE ESTADÍSTICAS
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
    //SNAPSHOT DE ESTADÍSTICAS

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

    @ManyToOne
    @JoinColumn(name = "clan_id")
    private Clan clan;

    @ManyToOne
    @JoinColumn(name = "arena_id")
    private Arena arena;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "league_stadistic_id")
    private LeagueStadistic leagueStatistics;

    @Column(columnDefinition = "TEXT")
    private String badges;

    @Column(columnDefinition = "TEXT")
    private String achievements;

    @OneToMany(mappedBy = "playerProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<PlayerCard> playerCards;

    // supportCards is a derived view over playerCards where supportCard == true
    public List<PlayerCard> getSupportCards() {
        if (this.playerCards == null) return null;
        return this.playerCards.stream().filter(c -> Boolean.TRUE.equals(c.getSupportCard())).collect(Collectors.toList());
    }

    public void setSupportCards(List<PlayerCard> supportCards) {
        if (supportCards == null) return;
        if (this.playerCards == null) this.playerCards = new ArrayList<>();
        for (PlayerCard pc : supportCards) {
            pc.setSupportCard(true);
            pc.setPlayerProfile(this);
            this.playerCards.add(pc);
        }
    }

    @ManyToOne
    @JoinColumn(name = "current_deck_id")
    private Deck currentDeck;

    @ManyToOne
    @JoinColumn(name = "current_deck_support_cards_id")
    private Deck currentDeckSupportCards;

    @OneToOne
    @JoinColumn(name = "current_favourite_card_id")
    private PlayerCard currentFavouriteCard;

    @Column(columnDefinition = "TEXT")
    private String currentPathOfLegendSeasonResult;

    @Column(columnDefinition = "TEXT")
    private String lastPathOfLegendSeasonResult;

    @Column(columnDefinition = "TEXT")
    private String bestPathOfLegendSeasonResult;

    @Column(columnDefinition = "TEXT")
    private String progress;


}
