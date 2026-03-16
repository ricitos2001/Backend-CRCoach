package org.example.backendcrcoach.domain.entities.principal_system_entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "sesions")
public class Session {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    @Column(nullable = false)
    private String title;
    private String notes;
    private String mood;
    @Column(nullable = false)
    private LocalDateTime startTime;
    @Column(nullable = false)
    private LocalDateTime endTime;
    @Column(nullable = false)
    private LocalDateTime createdAt;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
