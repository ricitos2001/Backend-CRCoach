package org.example.backendcrcoach.domain.dto.other_dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PasswordForgotRequestDTO {
    @NotBlank
    @Email
    private String email;
}
