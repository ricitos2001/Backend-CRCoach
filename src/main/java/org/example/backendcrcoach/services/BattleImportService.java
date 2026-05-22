package org.example.backendcrcoach.services;

import org.example.backendcrcoach.domain.entities.Battle;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import tools.jackson.databind.JsonNode;

@Service
public class BattleImportService {

    private final BattleService battleService;

    public BattleImportService(@Lazy BattleService battleService) {
        this.battleService = battleService;
    }

    @org.springframework.transaction.annotation.Transactional(propagation = Propagation.REQUIRES_NEW)
    public Battle importSingleBattle(JsonNode node) {
        return battleService.importSingleBattleFromNode(node);
    }
}
