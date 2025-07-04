package msla.challenge.mslachallenge.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import msla.challenge.mslachallenge.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.username = :username AND u.isActive = true")
    Optional<User> findActiveUserByUsername(String username);

    @Query("SELECT u FROM User u WHERE u.isActive = true")
    List<User> findAllActiveUsers();
}