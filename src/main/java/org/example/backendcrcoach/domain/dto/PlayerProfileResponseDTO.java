package org.example.backendcrcoach.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.backendcrcoach.domain.entities.PlayerCard;
import java.util.List;

@Getter
@AllArgsConstructor
public class PlayerProfileResponseDTO {
    private Long id;
    private String tag;
    private String name;
    private Integer expLevel;
    private Integer trophies;
    private Integer bestTrophies;
    private Integer wins;
    private Integer losses;
    private Integer battleCount;
    private Integer threeCrownWins;
    private Integer challengeCardsWon;
    private Integer challengeMaxWins;
    private Integer tournamentCardsWon;
    private Integer tournamentBattleCount;
    private String role;
    private Integer donations;
    private Integer donationsReceived;
    private Integer totalDonations;
    private Integer warDayWins;
    private Integer clanCardsCollected;
    private Integer starPoints;
    private Integer expPoints;
    private Integer legacyTrophyRoadHighScore;
    private Integer totalExpPoints;

    private String clan;
    private String arena;
    private String leagueStatistics;
    private String badges;
    private String achievements;
    private List<PlayerCard> playerCards;
    private List<PlayerCard> supportCards;
    private String currentDeck;
    private String currentDeckSupportCards;
    private String currentFavouriteCard;
    private String currentPathOfLegendSeasonResult;
    private String lastPathOfLegendSeasonResult;
    private String bestPathOfLegendSeasonResult;
    private String progress;
}

