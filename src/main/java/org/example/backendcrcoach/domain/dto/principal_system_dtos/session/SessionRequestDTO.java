package org.example.backendcrcoach.domain.dto.principal_system_dtos.session;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.backendcrcoach.domain.entities.principal_system_entities.User;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SessionRequestDTO {
    private String title;
    private String notes;
    private String mood;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime createdAt;
    private User user;
}
