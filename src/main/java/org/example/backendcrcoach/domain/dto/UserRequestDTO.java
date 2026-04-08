package org.example.backendcrcoach.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.backendcrcoach.domain.enums.Role;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserRequestDTO {
    private String name;
    private String surnames;
    private String username;
    private String email;
    private String phoneNumber;
    private String passwordHash;
    private String avatarUrl;
    private Role role;
    private LocalDateTime createdAt;
    private String playerTag;
    private Boolean enabled;
}
