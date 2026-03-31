package org.example.backendcrcoach.domain.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String message;
    private Date createdAt;
    @Column(name = "user_email", nullable = false)
    private String userEmail;
    @Column(name = "read", nullable = false)
    private Boolean read;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = new Date();
        if (read == null) read = false;
    }
}

