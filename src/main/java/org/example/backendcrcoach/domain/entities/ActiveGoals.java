package org.example.backendcrcoach.domain.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "active_goals")
public class ActiveGoals {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private Integer count;
    private String nearestDeadline;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "most_advanced_id")
    private MostAdvanced mostAdvanced;
}

