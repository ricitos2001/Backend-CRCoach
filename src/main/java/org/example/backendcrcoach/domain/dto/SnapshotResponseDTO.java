package org.example.backendcrcoach.domain.dto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SnapshotResponseDTO {
    private Long id;
    @JsonProperty("playerTag")
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
    private LocalDateTime capturedAt;
}
