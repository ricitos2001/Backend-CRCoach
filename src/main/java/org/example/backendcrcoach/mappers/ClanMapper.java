package org.example.backendcrcoach.mappers;

import org.example.backendcrcoach.domain.dto.ClanRequestDTO;
import org.example.backendcrcoach.domain.dto.ClanResponseDTO;
import org.example.backendcrcoach.domain.entities.Clan;

public class ClanMapper {

    public static Clan toEntity(ClanRequestDTO dto) {
        Clan clan = new Clan();
        clan.setTag(dto.getTag());
        clan.setName(dto.getName());
        // If the request supplies a badgeId (from the Clash Royale API), use it.
        // This ensures we store the real external badge id instead of a DB-generated one.
        clan.setBadgeId(dto.getBadgeId());
        return clan;
    }

    public static ClanResponseDTO toDTO(Clan clan) {
        return new ClanResponseDTO(
                clan.getTag(),
                clan.getName(),
                clan.getBadgeId()
        );
    }
}

