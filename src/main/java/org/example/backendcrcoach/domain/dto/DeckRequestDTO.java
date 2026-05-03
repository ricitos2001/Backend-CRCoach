package org.example.backendcrcoach.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.backendcrcoach.analytics.Archetype;
import org.example.backendcrcoach.domain.entities.PlayerCard;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeckRequestDTO {
    private Archetype archetype;
    private Integer deckIndex;
    private List<PlayerCard> playerCards;
}

