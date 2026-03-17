package org.example.backendcrcoach.domain.dto.security_dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PasswordForgotRequestDTO {
    @NotBlank
    @Email
    private String email;
}
