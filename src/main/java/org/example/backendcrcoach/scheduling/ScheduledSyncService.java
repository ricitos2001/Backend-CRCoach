package org.example.backendcrcoach.scheduling;

import org.example.backendcrcoach.domain.entities.PlayerProfile;
import org.example.backendcrcoach.repositories.PlayerProfileRepository;
import org.example.backendcrcoach.services.PlayerProfileService;
import org.example.backendcrcoach.services.CardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ScheduledSyncService {

    private static final Logger log = LoggerFactory.getLogger(ScheduledSyncService.class);

    private final PlayerProfileRepository playerProfileRepository;
    private final PlayerProfileService playerProfileService;
    private final CardService cardService;

    public ScheduledSyncService(PlayerProfileRepository playerProfileRepository,
                                PlayerProfileService playerProfileService,
                                CardService cardService) {
        this.playerProfileRepository = playerProfileRepository;
        this.playerProfileService = playerProfileService;
        this.cardService = cardService;
    }

    @Scheduled(fixedDelayString = "${sync.profiles.fixedDelayMs:300000}")
    public void syncProfilesAndCreateSnapshots() {
        try {
            List<PlayerProfile> profiles = playerProfileRepository.findAll();
            log.info("Scheduled sync: found {} profiles to check", profiles.size());
            for (PlayerProfile profile : profiles) {
                try {
                    // Use playerProfileService.getPlayer which already fetches from API, saves profile and creates snapshot
                    // But we want to avoid creating duplicate snapshots when no changes: so fetch API manually and compare
                    String tag = profile.getTag();
                    if (tag == null || tag.isBlank()) continue;

                    // playerProfileService.getPlayer expects tag without '#'
                    String rawTag = tag.startsWith("#") ? tag.substring(1) : tag;
                    // Call API via the service flow which will save snapshot and import battles (getPlayer crea snapshot condicionada)
                    playerProfileService.getPlayer(rawTag);
                } catch (Exception e) {
                    log.warn("Failed to sync profile {}: {}", profile.getTag(), e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Error during scheduled profile sync: {}", e.getMessage(), e);
        }
    }

    // Sincronizar cartas periódicamente (por defecto cada 1 hora)
    @Scheduled(fixedDelayString = "${sync.cards.fixedDelayMs:3600000}")
    public void syncCardsFromApi() {
        try {
            int imported = cardService.importAllCardsFromApi();
            if (imported > 0) {
                log.info("Imported {} new cards from Clash API during scheduled sync", imported);
            } else {
                log.debug("No new cards imported during scheduled sync");
            }
        } catch (Exception e) {
            log.error("Error during scheduled cards sync: {}", e.getMessage(), e);
        }
    }

}

