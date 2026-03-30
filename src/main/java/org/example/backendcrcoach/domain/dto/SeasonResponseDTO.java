package org.example.backendcrcoach.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SeasonResponseDTO implements Serializable {

    @JsonProperty("id")
    private String seasonId;

    private Integer trophies;
    private Integer bestTrophies;
}

