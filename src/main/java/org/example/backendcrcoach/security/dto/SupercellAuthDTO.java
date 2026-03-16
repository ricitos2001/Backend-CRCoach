package org.example.backendcrcoach.security.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SupercellAuthDTO {
    @NotBlank(message = "El token de Supercell es obligatorio")
    private String supercellToken;

    // Opcional: tag del jugador que puede venir del cliente
    private String playerTag;
}

