package org.example.backendcrcoach.services;

import jakarta.transaction.Transactional;
import org.example.backendcrcoach.domain.dto.PlayerCardResponseDTO;
import org.example.backendcrcoach.domain.dto.PlayerDeckRequestDTO;
import org.example.backendcrcoach.domain.dto.PlayerDeckResponseDTO;
import org.example.backendcrcoach.domain.entities.Battle;
import org.example.backendcrcoach.domain.entities.IconUrl;
import org.example.backendcrcoach.domain.entities.PlayerCard;
import org.example.backendcrcoach.domain.entities.PlayerDeck;
import org.example.backendcrcoach.domain.entities.PlayerProfile;
import org.example.backendcrcoach.mappers.PlayerCardMapper;
import org.example.backendcrcoach.mappers.PlayerDeckMapper;
import org.example.backendcrcoach.repositories.*;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PlayerDeckService {

    private final PlayerDeckRepository playerDeckRepository;
    private final PlayerCardRepository playerCardRepository;
    private final PlayerProfileRepository playerProfileRepository;
    private final BattleRepository battleRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public PlayerDeckService(PlayerDeckRepository playerDeckRepository,
                             PlayerCardRepository playerCardRepository,
                             PlayerProfileRepository playerProfileRepository,
                             BattleRepository battleRepository) {
        this.playerDeckRepository = playerDeckRepository;
        this.playerCardRepository = playerCardRepository;
        this.playerProfileRepository = playerProfileRepository;
        this.battleRepository = battleRepository;
    }

    public List<PlayerDeckResponseDTO> listAll() {
        return playerDeckRepository.findAll().stream().map(PlayerDeckMapper::toDTO).collect(Collectors.toList());
    }

    public PlayerDeckResponseDTO create(PlayerDeckRequestDTO dto) {
        if (dto.getPlayerCardIds() == null || dto.getPlayerCardIds().size() != 8) {
            throw new IllegalArgumentException("El mazo debe contener exactamente 8 cartas (playerCardIds)");
        }

        List<PlayerCard> cards = new ArrayList<>();
        for (Long id : dto.getPlayerCardIds()) {
            playerCardRepository.findById(id).ifPresent(cards::add);
        }

        String normalizedTag = normalizeTag(dto.getPlayerTag());
        PlayerProfile profile = null;
        if (normalizedTag != null) {
            profile = playerProfileRepository.findByTag(normalizedTag).orElse(null);
        }

        PlayerDeck deck = PlayerDeckMapper.toEntity(dto, cards, profile);
        PlayerDeck saved = playerDeckRepository.save(deck);
        return PlayerDeckMapper.toDTO(saved);
    }

    private String normalizeTag(String tag) {
        if (tag == null) return null;
        return tag.startsWith("#") ? tag : "#" + tag;
    }

    /**
     * Recorre las batallas almacenadas y extrae las cartas jugadas por el jugador (team/opponent)
     * e importa PlayerCard en la base de datos si no existen.
     */
    public List<PlayerCardResponseDTO> importCardsFromBattles(String playerTag) {
        String normalizedTag = normalizeTag(playerTag);
        if (normalizedTag == null) return List.of();

        List<Battle> battles = battleRepository.findAll();
        List<PlayerCard> saved = new ArrayList<>();

        for (Battle b : battles) {
            // team and opponent are stored as JSON text
            saved.addAll(processBattleSide(b.getTeam(), normalizedTag));
            saved.addAll(processBattleSide(b.getOpponent(), normalizedTag));
        }

        return saved.stream().map(PlayerCardMapper::toDTO).collect(Collectors.toList());
    }

    private List<PlayerCard> processBattleSide(String jsonSide, String normalizedTag) {
        List<PlayerCard> result = new ArrayList<>();
        if (jsonSide == null || jsonSide.isBlank()) return result;

        try {
            JsonNode arr = objectMapper.readTree(jsonSide);
            if (!arr.isArray()) return result;
            for (JsonNode member : arr) {
                JsonNode tagNode = member.get("tag");
                if (tagNode == null || tagNode.isNull()) continue;
                String tag = tagNode.asText();
                if (!normalizedTag.equals(tag)) continue;

                // process cards
                JsonNode cardsNode = member.get("cards");
                if (cardsNode != null && cardsNode.isArray()) {
                    for (JsonNode c : cardsNode) {
                        Integer cardId = c.has("id") && !c.get("id").isNull() ? c.get("id").asInt() : null;
                        if (cardId == null) continue;
                        if (playerCardRepository.existsByCardIdAndPlayerProfileTag(cardId, normalizedTag)) continue;

                        PlayerCard card = new PlayerCard();
                        card.setCardId(cardId);
                        card.setName(c.has("name") && !c.get("name").isNull() ? c.get("name").asText() : null);
                        card.setLevel(c.has("level") && !c.get("level").isNull() ? c.get("level").asInt() : null);
                        card.setMaxLevel(c.has("maxLevel") && !c.get("maxLevel").isNull() ? c.get("maxLevel").asInt() : null);
                        card.setMaxEvolutionLevel(c.has("maxEvolutionLevel") && !c.get("maxEvolutionLevel").isNull() ? c.get("maxEvolutionLevel").asInt() : null);
                        card.setRarity(c.has("rarity") && !c.get("rarity").isNull() ? c.get("rarity").asText() : null);
                        card.setCount(null);
                        card.setElixirCost(c.has("elixirCost") && !c.get("elixirCost").isNull() ? c.get("elixirCost").asInt() : null);

                        JsonNode iconNode = c.get("iconUrls");
                        if (iconNode != null && !iconNode.isNull()) {
                            IconUrl icon = new IconUrl();
                            icon.setMedium(iconNode.has("medium") && !iconNode.get("medium").isNull() ? iconNode.get("medium").asText() : null);
                            icon.setEvolutionMedium(iconNode.has("evolutionMedium") && !iconNode.get("evolutionMedium").isNull() ? iconNode.get("evolutionMedium").asText() : null);
                            card.setIconUrl(icon);
                        }

                        card.setSupportCard(false);
                        playerProfileRepository.findByTag(normalizedTag).ifPresent(card::setPlayerProfile);
                        result.add(playerCardRepository.save(card));
                    }
                }

                // supportCards (if any)
                JsonNode supportNode = member.get("supportCards");
                if (supportNode != null && supportNode.isArray()) {
                    for (JsonNode c : supportNode) {
                        Integer cardId = c.has("id") && !c.get("id").isNull() ? c.get("id").asInt() : null;
                        if (cardId == null) continue;
                        if (playerCardRepository.existsByCardIdAndPlayerProfileTag(cardId, normalizedTag)) continue;

                        PlayerCard card = new PlayerCard();
                        card.setCardId(cardId);
                        card.setName(c.has("name") && !c.get("name").isNull() ? c.get("name").asText() : null);
                        card.setLevel(c.has("level") && !c.get("level").isNull() ? c.get("level").asInt() : null);
                        card.setMaxLevel(c.has("maxLevel") && !c.get("maxLevel").isNull() ? c.get("maxLevel").asInt() : null);
                        card.setMaxEvolutionLevel(c.has("maxEvolutionLevel") && !c.get("maxEvolutionLevel").isNull() ? c.get("maxEvolutionLevel").asInt() : null);
                        card.setRarity(c.has("rarity") && !c.get("rarity").isNull() ? c.get("rarity").asText() : null);
                        card.setCount(null);
                        card.setElixirCost(c.has("elixirCost") && !c.get("elixirCost").isNull() ? c.get("elixirCost").asInt() : null);

                        JsonNode iconNode = c.get("iconUrls");
                        if (iconNode != null && !iconNode.isNull()) {
                            IconUrl icon = new IconUrl();
                            icon.setMedium(iconNode.has("medium") && !iconNode.get("medium").isNull() ? iconNode.get("medium").asText() : null);
                            icon.setEvolutionMedium(iconNode.has("evolutionMedium") && !iconNode.get("evolutionMedium").isNull() ? iconNode.get("evolutionMedium").asText() : null);
                            card.setIconUrl(icon);
                        }

                        card.setSupportCard(true);
                        playerProfileRepository.findByTag(normalizedTag).ifPresent(card::setPlayerProfile);
                        result.add(playerCardRepository.save(card));
                    }
                }
            }
        } catch (Exception e) {
            // ignore malformed JSON for a given battle side
        }

        return result;
    }
}

