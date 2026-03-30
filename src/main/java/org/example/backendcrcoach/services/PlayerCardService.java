package org.example.backendcrcoach.services;

import jakarta.transaction.Transactional;
import org.example.backendcrcoach.domain.dto.PlayerCardRequestDTO;
import org.example.backendcrcoach.domain.dto.PlayerCardResponseDTO;
import org.example.backendcrcoach.domain.entities.IconUrl;
import org.example.backendcrcoach.domain.entities.PlayerCard;
import org.example.backendcrcoach.domain.entities.PlayerProfile;
import org.example.backendcrcoach.mappers.PlayerCardMapper;
import org.example.backendcrcoach.repositories.PlayerCardRepository;
import org.example.backendcrcoach.repositories.PlayerProfileRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.example.backendcrcoach.config.WebClientHelper;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class PlayerCardService {

    private final PlayerCardRepository playerCardRepository;
    private final PlayerProfileRepository playerProfileRepository;
    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final WebClientHelper webClientHelper;

    public PlayerCardService(PlayerCardRepository playerCardRepository,
                             PlayerProfileRepository playerProfileRepository,
                             WebClient.Builder builder,
                             @Value("${clash.royale.api.url}") String API_URL,
                             @Value("${clash.royale.api.key}") String API_KEY,
                             WebClientHelper webClientHelper) {
        this.playerCardRepository = playerCardRepository;
        this.playerProfileRepository = playerProfileRepository;
        this.webClient = builder.baseUrl(API_URL).defaultHeader("Authorization", "Bearer " + API_KEY).build();
        this.webClientHelper = webClientHelper;
    }

    public List<PlayerCardResponseDTO> listAll() {
        return playerCardRepository.findAll().stream().map(PlayerCardMapper::toDTO).collect(Collectors.toList());
    }

    public PlayerCardResponseDTO create(PlayerCardRequestDTO dto) {
        PlayerCard card = PlayerCardMapper.toEntity(dto);
        PlayerCard saved = playerCardRepository.save(card);
        return PlayerCardMapper.toDTO(saved);
    }

    public List<PlayerCardResponseDTO> importCardsForPlayer(String playerTag) {
        String response = webClientHelper.fetchGetWithRetries(webClient, "/players/{tag}", "#" + playerTag);
        if (response == null || response.isBlank()) return List.of();

        JsonNode root;
        try {
            root = objectMapper.readTree(response);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("Invalid response from player API", e);
        }

        List<PlayerCard> saved = new java.util.ArrayList<>();

        // process regular cards
        JsonNode cardsNode = root.get("cards");
        if (cardsNode != null && cardsNode.isArray()) {
            for (JsonNode node : cardsNode) {
                Integer cardId = node.has("id") && !node.get("id").isNull() ? node.get("id").asInt() : null;
                if (cardId == null) continue;
                if (playerCardRepository.existsByCardIdAndPlayerProfileTag(cardId, "#" + playerTag)) continue;
                PlayerCard card = parseCards(node);
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
                PlayerCard card = parseCards(node);
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

    public PlayerCard parseCards(JsonNode s) {
        PlayerCard card = new PlayerCard();
        card.setCardId(s.has("id") && !s.get("id").isNull() ? s.get("id").asInt() : null);
        card.setName(s.has("name") && !s.get("name").isNull() ? s.get("name").asString() : null);
        card.setLevel(s.has("level") && !s.get("level").isNull() ? s.get("level").asInt() : null);
        card.setMaxLevel(s.has("maxLevel") && !s.get("maxLevel").isNull() ? s.get("maxLevel").asInt() : null);
        card.setMaxEvolutionLevel(s.has("maxEvolutionLevel") && !s.get("maxEvolutionLevel").isNull() ? s.get("maxEvolutionLevel").asInt() : null);
        card.setRarity(s.has("rarity") && !s.get("rarity").isNull() ? s.get("rarity").asString() : null);
        card.setCount(s.has("count") && !s.get("count").isNull() ? s.get("count").asInt() : null);
        card.setElixirCost(s.has("elixirCost") && !s.get("elixirCost").isNull() ? s.get("elixirCost").asInt() : null);
        JsonNode iconNode2 = s.get("iconUrls");
        if (iconNode2 != null && !iconNode2.isNull()) {
            IconUrl iconUrl2 = new IconUrl();

            iconUrl2.setMedium(iconNode2.has("medium") && !iconNode2.get("medium").isNull() ? iconNode2.get("medium").asString() : null);
            iconUrl2.setEvolutionMedium(iconNode2.has("evolutionMedium") && !iconNode2.get("evolutionMedium").isNull() ? iconNode2.get("evolutionMedium").asText() : null);
            card.setIconUrl(iconUrl2);
        }
        return card;
    }
    public void saveCardsFromProfile(PlayerProfile profile) {
        if (profile == null || profile.getPlayerCards() == null) return;

        // Obtener cardIds existentes para este playerTag para evitar múltiples consultas por cada carta
        List<PlayerCard> existing = playerCardRepository.findByPlayerProfileTag(profile.getTag());
        Set<Integer> existingIds = existing.stream()
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

    /**
     * Persiste (o reutiliza) la carta favorita antes de asignarla al PlayerProfile
     * para evitar referencias transientes. Si la carta ya tiene id se devuelve tal cual.
     * Si existe una carta con el mismo cardId para ese perfil se reutiliza.
     */
    public PlayerCard persistFavouriteCardIfNeeded(PlayerProfile profile, PlayerCard card) {
        if (card == null) return null;
        if (card.getId() != null) return card;

        Integer cid = card.getCardId();
        if (cid != null && profile != null) {
            // Buscar entre las cartas ya guardadas para este perfil
            List<PlayerCard> existing = playerCardRepository.findByPlayerProfileTag(profile.getTag());
            for (PlayerCard pc : existing) {
                if (cid.equals(pc.getCardId())) return pc;
            }
        }

        // Asociar al perfil si está disponible (si el perfil no está guardado aún, la columna player_profile_id
        // puede quedar a NULL; más adelante se re-asociará si es necesario).
        if (profile != null) card.setPlayerProfile(profile);
        return playerCardRepository.save(card);
    }
}

