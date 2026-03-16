package org.example.backendcrcoach.domain.entities.principal_system_entities;

import jakarta.persistence.*;
import lombok.*;
import org.example.backendcrcoach.domain.enums.GoalStatus;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "goals")
public class Goal {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    @Column(nullable = false)
    private String title;
    @Column(nullable = false)
    private String description;
    @Column(nullable = false)
    private String metricType;
    @Column(nullable = false)
    private Double targetValue;
    @Column(nullable = false)
    private Double currentValue;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private GoalStatus status;
    @Column(nullable = false)
    private String deadline;
    @Column(nullable = false)
    private LocalDateTime createdAt;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
