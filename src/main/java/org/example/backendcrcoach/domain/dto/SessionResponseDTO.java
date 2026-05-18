package org.example.backendcrcoach.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.backendcrcoach.domain.entities.User;

@Getter
@AllArgsConstructor
public class SessionResponseDTO {
    private Long id;
    private String title;
    private String notes;
    private String mood;
    private String enfoque;
    private String startTime;
    private String endTime;
    private String createdAt;
    private User user;
}
