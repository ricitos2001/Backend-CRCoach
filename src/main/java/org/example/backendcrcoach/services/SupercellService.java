package org.example.backendcrcoach.services;

import org.example.backendcrcoach.domain.entities.User;
import org.example.backendcrcoach.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class SupercellService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final UserRepository userRepository;

    @org.springframework.beans.factory.annotation.Value("${clash.royale.api.url:https://api.clashroyale.com/v1}")
    private String supercellBaseUrl;

    @org.springframework.beans.factory.annotation.Value("${clash.royale.api.key:}")
    private String developerToken;

    public SupercellService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Valida un token con la API de Supercell (ejemplo para Clash Royale) y devuelve datos básicos del jugador.
     * Nota: la API oficial de Supercell usa un token de desarrollador para acceder a datos públicos. Si el flujo
     * que se requiere es distinto (OAuth) habrá que adaptarlo. Aquí tratamos el token recibido como identificador
     * de jugador que el cliente puede enviar y consultamos /players/{playerTag} como ejemplo.
     */
    public Map<String, Object> getPlayerInfoByTag(String playerTag, String providedToken) {
        // Asegurar que el playerTag esté correctamente codificado para la URL
        String encodedTag = java.net.URLEncoder.encode(playerTag, java.nio.charset.StandardCharsets.UTF_8);
        String url = supercellBaseUrl + "/players/" + encodedTag;
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        String tokenToUse = (providedToken != null && !providedToken.isEmpty()) ? providedToken : this.developerToken;
        headers.set("Authorization", "Bearer " + tokenToUse);
        org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(headers);

        org.springframework.http.ResponseEntity<java.util.Map<String, Object>> response = restTemplate.exchange(url, org.springframework.http.HttpMethod.GET, entity, (Class) java.util.Map.class);
        //noinspection unchecked
        return response.getBody();
    }

    public User findOrCreateUserFromSupercellData(Map<String, Object> playerData) {
        if (playerData == null) return null;
        String tag = (String) playerData.get("tag");
        if (tag == null) return null;

        // Buscar usuario por playerTag
        return userRepository.findByPlayerTag(tag)
                .orElseGet(() -> {
                    User newUser = new User();
                    // mapear campos mínimos y asegurar unicidad de username/email
                    newUser.setPlayerTag(tag);
                    String sanitizedTag = tag.replaceAll("#", "");
                    String name = (String) playerData.getOrDefault("name", "sc_" + sanitizedTag);
                    String baseUsername = name.toLowerCase().replaceAll("\\s+", "_");
                    String username = baseUsername;
                    int suffix = 1;
                    while (userRepository.existsByUsername(username)) {
                        username = baseUsername + "_" + suffix++;
                    }
                    newUser.setUsername(username);
                    String email = sanitizedTag.toLowerCase() + "@supercell.local";
                    newUser.setEmail(email);
                    // Generar un passwordHash no nulo para cumplir con la columna NOT NULL
                    newUser.setPasswordHash("SOCIAL_LOGIN_" + java.util.UUID.randomUUID());
                    newUser.setRole(org.example.backendcrcoach.domain.enums.Role.USER);
                    newUser.setCreatedAt(java.time.LocalDateTime.now());
                    newUser.setEnabled(true);
                    return userRepository.save(newUser);
                });
    }
}

