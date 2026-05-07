# Backend CRCoach
API del proyecto CRCoach para el proyecto CRCoach.

## Diagrama de Entidades y Relaciones (ER)
```mermaid
erDiagram
%% Entidades principales
    USER {
        Long id PK
        String username
        String email
        String passwordHash
        String avatarUrl
        String role
        Boolean enabled
        DateTime createdAt
        String playerTag
    }

    PLAYER_PROFILE {
        Long id PK
        String playerTag
        String playerName
        Integer arena
        Integer trophies
        Integer bestTrophies
        Integer level
        String clanName
        DateTime lastSyncAt
        Long user_id FK
    }

    SNAPSHOT {
        Long id PK
        Integer trophies
        Integer wins
        Integer losses
        Integer battleCount
        Integer threeCrownWins
        DateTime timestamp
        Long player_profile_id FK
    }

    BATTLE {
        Long id PK
        DateTime battleTime
        String gameMode
        String result
        Integer crowns
        Integer opponentCrowns
        Integer trophyChange
        Long player_profile_id FK
        Long archetype_id FK
    }

    DECK {
        Long id PK
        Long battle_id FK
        String type
    }

    DECK_CARD {
        Long id PK
        Long deck_id FK
        Long card_id FK
        Integer level
        Integer position
    }

    CARD {
        Long id PK
        String name
        Integer maxLevel
        String iconUrl
        String rarity
        Integer elixirCost
    }

    ARCHETYPE {
        Long id PK
        String name
        String description
    }

    GOAL {
        Long id PK
        String title
        String description
        String metricType
        Float targetValue
        Float currentValue
        String status
        DateTime deadline
        DateTime createdAt
        Long user_id FK
    }

    SESSION {
        Long id PK
        String title
        String notes
        String mood
        DateTime startTime
        DateTime endTime
        DateTime createdAt
        Long user_id FK
    }

    SYNC_LOG {
        Long id PK
        String playerTag
        DateTime syncDate
        Integer battlesFetched
        String status
        String errorMessage
        Long player_profile_id FK
    }

%% Relaciones
    USER ||--o{ PLAYER_PROFILE : "tiene"
    USER ||--o{ GOAL : "posee"
    USER ||--o{ SESSION : "registra"

    PLAYER_PROFILE ||--o{ SNAPSHOT : "tiene"
    PLAYER_PROFILE ||--o{ BATTLE : "participa"
    PLAYER_PROFILE ||--o{ SYNC_LOG : "registra"

    BATTLE ||--o{ DECK : "usa"
    DECK ||--o{ DECK_CARD : "contiene"
    DECK_CARD }o--|| CARD : "corresponde a"

    BATTLE }o--|| ARCHETYPE : "clasificado como"
```

## peticion de prueba con curl
```bash
curl -X GET "https://api.clashroyale.com/v1/players/%23${CLASH_ROYALE_PLAYER_TAG}" -H "Authorization: Bearer ${CLASH_ROYALE_API_KEY}"
```

