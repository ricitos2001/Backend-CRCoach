package org.example.backendcrcoach.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClanRequestDTO {
    private String tag;
    private String name;
    // badgeId is optional on create requests but when available it should be the
    // id provided by the Clash Royale API so we persist the real value.
    private Long badgeId;
}

