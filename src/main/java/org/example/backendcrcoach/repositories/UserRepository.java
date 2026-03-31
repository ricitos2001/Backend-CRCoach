
package org.example.backendcrcoach.repositories;

import org.example.backendcrcoach.domain.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    User getUsersById(Long id);

    User getUserByUsername(String username);

    User getUserByEmail(String email);

    Optional<User> findByEmail(String email);

    Boolean existsByUsername(String username);

    Boolean existsByUsernameAndIdNot(String username, Long id);

    Boolean existsByEmailAndIdNot(String email, Long id);

    Optional<User> findByUsername(String username);

    Optional<User> findUserByEmail(String email);

    Optional<User> findByPlayerTag(String playerTag);

    Boolean existsByPlayerTagAndIdNot(String playerTag, Long id);
}
