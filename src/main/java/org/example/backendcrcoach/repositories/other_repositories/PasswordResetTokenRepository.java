package org.example.backendcrcoach.repositories.other_repositories;

import org.example.backendcrcoach.domain.entities.other_entities.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByTokenHash(String tokenHash);
    void deleteByExpiresAtBefore(Instant time);
}
