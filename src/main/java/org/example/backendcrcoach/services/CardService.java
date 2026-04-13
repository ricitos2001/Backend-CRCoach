package org.example.backendcrcoach.services;

import jakarta.transaction.Transactional;
import org.example.backendcrcoach.domain.dto.CardRequestDTO;
import org.example.backendcrcoach.domain.dto.CardResponseDTO;
import org.example.backendcrcoach.domain.entities.Card;
import org.example.backendcrcoach.domain.entities.IconUrl;
import org.example.backendcrcoach.mappers.CardMapper;
import org.example.backendcrcoach.repositories.CardRepository;
import org.example.backendcrcoach.repositories.PlayerProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.example.backendcrcoach.config.WebClientHelper;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class CardService {

    private static final Logger log = LoggerFactory.getLogger(CardService.class);

    private final CardRepository cardRepository;private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final WebClientHelper webClientHelper;

    public CardService(CardRepository cardRepository,
                       WebClient.Builder builder,
                       @Value("${clash.royale.api.url}") String API_URL,
                       @Value("${clash.royale.api.key}") String API_KEY,
                       WebClientHelper webClientHelper) {
        this.cardRepository = cardRepository;
        this.webClient = builder
                .baseUrl(API_URL)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + API_KEY)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.USER_AGENT, "CRCoach/Backend-CRCoach")
                .build();
        this.webClientHelper = webClientHelper;
    }

    public CardResponseDTO create(CardRequestDTO dto) {
        Card card = CardMapper.toEntity(dto);
        Card saved = cardRepository.save(card);
        return CardMapper.toDTO(saved);
    }

    public List<CardResponseDTO> listAll() {
        return cardRepository.findAll().stream().map(CardMapper::toDTO).collect(Collectors.toList());
    }

    public Optional<CardResponseDTO> findById(Long id) {
        return cardRepository.findById(id).map(CardMapper::toDTO);
    }

    public void delete(Long id) {
        cardRepository.deleteById(id);
    }

    public int importAllCardsFromApi() {
        String response = webClientHelper.fetchGetWithRetries(webClient, "/cards");
        if (response == null || response.isBlank()) return 0;
        JsonNode root;
        try {
            root = objectMapper.readTree(response);
        } catch (RuntimeException e) {
            log.error("Invalid JSON from cards API: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid response from cards API", e);
        }

        // Support multiple response shapes: direct array or object containing items/cards
        JsonNode arrayNode;
        if (root.isArray()) {
            arrayNode = root;
        } else if (root.has("items") && root.get("items").isArray()) {
            arrayNode = root.get("items");
        } else if (root.has("cards") && root.get("cards").isArray()) {
            arrayNode = root.get("cards");
        } else {
            log.warn("cards API returned unexpected JSON shape: {}", root);
            return 0;
        }

        int imported = 0;
        for (JsonNode node : arrayNode) {
            Card card = createCardFromNode(node);
            if (card == null) continue;
            cardRepository.save(card);
            imported++;
        }

        if (root.has("supportItems") && root.get("supportItems").isArray()) {
            for (JsonNode node : root.get("supportItems")) {
                Card card = createCardFromNode(node);
                if (card == null) continue;
                cardRepository.save(card);
                imported++;
            }
        }

        log.info("Imported {} cards from API", imported);
        return imported;
    }

    private String readText(JsonNode node, String field) {
        JsonNode v = node.get(field);
        return v == null || v.isNull() ? null : objectMapper.convertValue(v, String.class);
    }

    private Integer readInteger(JsonNode node, String field) {
        JsonNode v = node.get(field);
        return v == null || v.isNull() ? null : v.asInt();
    }

    private Card createCardFromNode(JsonNode node) {
        Integer cardId = readInteger(node, "id");
        if (cardId == null) return null;
        if (cardRepository.existsByCardId(cardId)) return null;

        Card card = new Card();
        card.setCardId(cardId);
        card.setName(readText(node, "name"));
        card.setMaxLevel(readInteger(node, "maxLevel"));
        card.setMaxEvolutionLevel(readInteger(node, "maxEvolutionLevel"));
        card.setRarity(readText(node, "rarity"));
        card.setElixirCost(readInteger(node, "elixirCost"));

        JsonNode iconNode = node.get("iconUrls");
        if (iconNode != null && !iconNode.isNull()) {
            IconUrl icon = new IconUrl();
            icon.setMedium(readText(iconNode, "medium"));
            icon.setHeroMedium(readText(iconNode, "heroMedium"));
            icon.setEvolutionMedium(readText(iconNode, "evolutionMedium"));
            card.setIconUrl(icon);
        }

        return card;
    }
}

