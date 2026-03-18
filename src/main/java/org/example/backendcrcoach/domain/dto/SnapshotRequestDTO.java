package org.example.backendcrcoach.domain.dto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SnapshotRequestDTO {
    // Identificador del perfil: se puede usar playerProfileId o playerTag (se prioriza playerProfileId si ambos presentes)
    private Long playerProfileId;
    private String playerTag;
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
    private Integer donations;
    private Integer donationsReceived;
    private Integer totalDonations;
    private Integer warDayWins;
    private Integer clanCardsCollected;
    private Integer starPoints;
    private Integer expPoints;
}
