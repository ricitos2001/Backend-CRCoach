package org.example.backendcrcoach.domain.dto.principal_system_dtos.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.backendcrcoach.domain.enums.Role;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class UserResponseDTO {
    private Long id;
    private String username;
    private String email;
    private String passwordHash;
    private String avatarUrl;
    private Role role;
    private LocalDateTime createdAt;
    private String playerProfiles;
    private Boolean enabled;
}
