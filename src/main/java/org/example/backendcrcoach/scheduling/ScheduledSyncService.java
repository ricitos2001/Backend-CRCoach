package org.example.backendcrcoach.scheduling;

import org.example.backendcrcoach.domain.entities.PlayerProfile;
import org.example.backendcrcoach.repositories.PlayerProfileRepository;
import org.example.backendcrcoach.services.PlayerProfileService;
import org.example.backendcrcoach.services.CardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ScheduledSyncService {

    private static final Logger log = LoggerFactory.getLogger(ScheduledSyncService.class);

    private final PlayerProfileRepository playerProfileRepository;
    private final PlayerProfileService playerProfileService;
    private final CardService cardService;
    private final UserSchedulingService userSchedulingService;

    public ScheduledSyncService(PlayerProfileRepository playerProfileRepository,
                                PlayerProfileService playerProfileService,
                                CardService cardService,
                                UserSchedulingService userSchedulingService) {
        this.playerProfileRepository = playerProfileRepository;
        this.playerProfileService = playerProfileService;
        this.cardService = cardService;
        this.userSchedulingService = userSchedulingService;
    }

    public void syncProfilesAndCreateSnapshots() {
        // Método global que solo se ejecuta cuando no hay scheduling por usuario activo
        if (userSchedulingService != null && userSchedulingService.hasActiveTasks()) {
            log.debug("Skipping global scheduled sync because per-user tasks are active");
            return;
        }
        try {
            List<PlayerProfile> profiles = playerProfileRepository.findAll();
            log.info("Scheduled sync: found {} profiles to check", profiles.size());
            for (PlayerProfile profile : profiles) {
                try {
                    String tag = profile.getTag();
                    if (tag == null || tag.isBlank()) continue;
                    String rawTag = tag.startsWith("#") ? tag.substring(1) : tag;
                    playerProfileService.getPlayer(rawTag);
                } catch (Exception e) {
                    log.warn("Failed to sync profile {}: {}", profile.getTag(), e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Error during scheduled profile sync: {}", e.getMessage(), e);
        }
    }

    // Permite sincronizar un solo perfil (usado por UserSchedulingService)
    void syncSingleProfile(String rawTag) {
        try {
            playerProfileService.getPlayer(rawTag);
        } catch (Exception e) {
            log.warn("Failed to sync single profile {}: {}", rawTag, e.getMessage());
        }
    }
    // Sincronizar cartas (método público para invocar cuando se requiera)
    public void syncCardsFromApi() {
        try {
            int imported = cardService.importAllCardsFromApi();
            if (imported > 0) {
                log.info("Imported {} new cards from Clash API during manual sync", imported);
            } else {
                log.debug("No new cards imported during manual sync");
            }
        } catch (Exception e) {
            log.error("Error during manual cards sync: {}", e.getMessage(), e);
        }
    }

}

