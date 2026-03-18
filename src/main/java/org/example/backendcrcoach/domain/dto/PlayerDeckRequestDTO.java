package org.example.backendcrcoach.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlayerDeckRequestDTO {
    private String name;
    // IDs de PlayerCard que componen el mazo
    private List<Long> playerCardIds;
    // Tag del perfil propietario (con o sin #)
    private String playerTag;
}

