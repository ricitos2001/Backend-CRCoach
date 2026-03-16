package org.example.backendcrcoach.domain.dto.principal_system_dtos.session;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.backendcrcoach.domain.entities.principal_system_entities.User;

@Getter
@AllArgsConstructor
public class SessionResponseDTO {
    private Long id;
    private String title;
    private String notes;
    private String mood;
    private String startTime;
    private String endTime;
    private String createdAt;
    private User user;
}
