package org.example.backendcrcoach.services;

import jakarta.transaction.Transactional;
import org.example.backendcrcoach.domain.dto.PlayerCardRequestDTO;
import org.example.backendcrcoach.domain.dto.PlayerCardResponseDTO;
import org.example.backendcrcoach.domain.entities.IconUrl;
import org.example.backendcrcoach.domain.entities.PlayerCard;
import org.example.backendcrcoach.mappers.PlayerCardMapper;
import org.example.backendcrcoach.repositories.PlayerCardRepository;
import org.example.backendcrcoach.repositories.PlayerProfileRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PlayerCardService {

    private final PlayerCardRepository playerCardRepository;
    private final PlayerProfileRepository playerProfileRepository;
    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public PlayerCardService(PlayerCardRepository playerCardRepository,
                             PlayerProfileRepository playerProfileRepository,
                             WebClient.Builder builder,
                             @Value("${clash.royale.api.url}") String API_URL,
                             @Value("${clash.royale.api.key}") String API_KEY) {
        this.playerCardRepository = playerCardRepository;
        this.playerProfileRepository = playerProfileRepository;
        this.webClient = builder.baseUrl(API_URL).defaultHeader("Authorization", "Bearer " + API_KEY).build();
    }

    public List<PlayerCardResponseDTO> listAll() {
        return playerCardRepository.findAll().stream().map(PlayerCardMapper::toDTO).collect(Collectors.toList());
    }

    public PlayerCardResponseDTO create(PlayerCardRequestDTO dto) {
        PlayerCard card = PlayerCardMapper.toEntity(dto);
        PlayerCard saved = playerCardRepository.save(card);
        return PlayerCardMapper.toDTO(saved);
    }

    public java.util.List<PlayerCardResponseDTO> importCardsForPlayer(String playerTag) {
        String response = webClient.get().uri("/players/{tag}", "#" + playerTag).retrieve().bodyToMono(String.class).block();
        if (response == null || response.isBlank()) return java.util.List.of();

        JsonNode root;
        try {
            root = objectMapper.readTree(response);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("Invalid response from player API", e);
        }

        java.util.List<PlayerCard> saved = new java.util.ArrayList<>();

        // process regular cards
        JsonNode cardsNode = root.get("cards");
        if (cardsNode != null && cardsNode.isArray()) {
            for (JsonNode node : cardsNode) {
                Integer cardId = node.has("id") && !node.get("id").isNull() ? node.get("id").asInt() : null;
                if (cardId == null) continue;
                if (playerCardRepository.existsByCardIdAndPlayerProfileTag(cardId, "#" + playerTag)) continue;

                PlayerCard card = new PlayerCard();
                card.setCardId(cardId);
                card.setName(node.has("name") && !node.get("name").isNull() ? objectMapper.convertValue(node.get("name"), String.class) : null);
                card.setLevel(node.has("level") && !node.get("level").isNull() ? node.get("level").asInt() : null);
                card.setMaxLevel(node.has("maxLevel") && !node.get("maxLevel").isNull() ? node.get("maxLevel").asInt() : null);
                card.setMaxEvolutionLevel(node.has("maxEvolutionLevel") && !node.get("maxEvolutionLevel").isNull() ? node.get("maxEvolutionLevel").asInt() : null);
                card.setRarity(node.has("rarity") && !node.get("rarity").isNull() ? objectMapper.convertValue(node.get("rarity"), String.class) : null);
                card.setCount(node.has("count") && !node.get("count").isNull() ? node.get("count").asInt() : null);
                card.setElixirCost(node.has("elixirCost") && !node.get("elixirCost").isNull() ? node.get("elixirCost").asInt() : null);

                JsonNode iconNode = node.get("iconUrls");
                if (iconNode != null && !iconNode.isNull()) {
                    IconUrl icon = new IconUrl();
                    icon.setMedium(iconNode.has("medium") && !iconNode.get("medium").isNull() ? objectMapper.convertValue(iconNode.get("medium"), String.class) : null);
                    icon.setEvolutionMedium(iconNode.has("evolutionMedium") && !iconNode.get("evolutionMedium").isNull() ? objectMapper.convertValue(iconNode.get("evolutionMedium"), String.class) : null);
                    card.setIconUrl(icon);
                }

                card.setSupportCard(false);
                playerProfileRepository.findByTag("#" + playerTag).ifPresent(card::setPlayerProfile);
                saved.add(playerCardRepository.save(card));
            }
        }

        // process supportCards (if any)
        JsonNode supportNode = root.get("supportCards");
        if (supportNode != null && supportNode.isArray()) {
            for (JsonNode node : supportNode) {
                Integer cardId = node.has("id") && !node.get("id").isNull() ? node.get("id").asInt() : null;
                if (cardId == null) continue;
                if (playerCardRepository.existsByCardIdAndPlayerProfileTag(cardId, "#" + playerTag)) continue;

                PlayerCard card = new PlayerCard();
                card.setCardId(cardId);
                card.setName(node.has("name") && !node.get("name").isNull() ? objectMapper.convertValue(node.get("name"), String.class) : null);
                card.setLevel(node.has("level") && !node.get("level").isNull() ? node.get("level").asInt() : null);
                card.setMaxLevel(node.has("maxLevel") && !node.get("maxLevel").isNull() ? node.get("maxLevel").asInt() : null);
                card.setMaxEvolutionLevel(node.has("maxEvolutionLevel") && !node.get("maxEvolutionLevel").isNull() ? node.get("maxEvolutionLevel").asInt() : null);
                card.setRarity(node.has("rarity") && !node.get("rarity").isNull() ? objectMapper.convertValue(node.get("rarity"), String.class) : null);
                card.setCount(node.has("count") && !node.get("count").isNull() ? node.get("count").asInt() : null);
                card.setElixirCost(node.has("elixirCost") && !node.get("elixirCost").isNull() ? node.get("elixirCost").asInt() : null);

                JsonNode iconNode = node.get("iconUrls");
                if (iconNode != null && !iconNode.isNull()) {
                    IconUrl icon = new IconUrl();
                    icon.setMedium(iconNode.has("medium") && !iconNode.get("medium").isNull() ? objectMapper.convertValue(iconNode.get("medium"), String.class) : null);
                    icon.setEvolutionMedium(iconNode.has("evolutionMedium") && !iconNode.get("evolutionMedium").isNull() ? objectMapper.convertValue(iconNode.get("evolutionMedium"), String.class) : null);
                    card.setIconUrl(icon);
                }

                card.setSupportCard(true);
                playerProfileRepository.findByTag("#" + playerTag).ifPresent(card::setPlayerProfile);
                saved.add(playerCardRepository.save(card));
            }
        }

        return saved.stream().map(PlayerCardMapper::toDTO).collect(Collectors.toList());
    }

    /**
     * Guarda las playerCards que ya estén asociadas en el objeto PlayerProfile.
     * Evita duplicados por cardId y asigna la relación bidireccional.
     */
    public void saveCardsFromProfile(org.example.backendcrcoach.domain.entities.PlayerProfile profile) {
        if (profile == null || profile.getPlayerCards() == null) return;

        // Obtener cardIds existentes para este playerTag para evitar múltiples consultas por cada carta
        List<PlayerCard> existing = playerCardRepository.findByPlayerProfileTag(profile.getTag());
        java.util.Set<Integer> existingIds = existing.stream()
                .map(PlayerCard::getCardId)
                .filter(java.util.Objects::nonNull)
                .collect(java.util.stream.Collectors.toSet());

        for (PlayerCard card : profile.getPlayerCards()) {
            Integer cid = card.getCardId();
            if (cid == null) continue;
            if (existingIds.contains(cid)) continue;
            card.setPlayerProfile(profile);
            PlayerCard saved = playerCardRepository.save(card);
            // Añadir la entidad guardada a la colección del perfil si no está presente
            if (profile.getPlayerCards() == null) {
                profile.setPlayerCards(new java.util.ArrayList<>());
            }
            boolean present = profile.getPlayerCards().stream().anyMatch(c -> cid.equals(c.getCardId()));
            if (!present) profile.getPlayerCards().add(saved);
            existingIds.add(cid);
        }
    }
}

