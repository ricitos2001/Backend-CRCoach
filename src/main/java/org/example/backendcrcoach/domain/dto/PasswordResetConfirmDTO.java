package org.example.backendcrcoach.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PasswordResetConfirmDTO {
    @NotBlank
    private String token;

    @NotBlank
    private String newPassword;
}
