package org.example.backendcrcoach.domain.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "clans")
public class Clan {
    @Column(name = "tag", unique = true)
    private String tag;
    @Column(nullable = false)
    private String name;
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long badgeId;
}

