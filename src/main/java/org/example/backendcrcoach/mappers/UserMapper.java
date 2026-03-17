package org.example.backendcrcoach.mappers;

import org.example.backendcrcoach.domain.dto.UserRequestDTO;
import org.example.backendcrcoach.domain.dto.UserResponseDTO;
import org.example.backendcrcoach.domain.entities.User;

public class UserMapper {
    public static User toEntity(UserRequestDTO dto) {
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPasswordHash(dto.getPasswordHash());
        user.setAvatarUrl(dto.getAvatarUrl());
        user.setRole(dto.getRole());
        user.setCreatedAt(dto.getCreatedAt());
        user.setPlayerTag(dto.getPlayerTag());
        user.setEnabled(dto.getEnabled());
        return user;
    }

    public static UserResponseDTO toDTO(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getAvatarUrl(),
                user.getRole(),
                user.getCreatedAt(),
                user.getPlayerTag(),
                user.getEnabled()
        );
    }
}
