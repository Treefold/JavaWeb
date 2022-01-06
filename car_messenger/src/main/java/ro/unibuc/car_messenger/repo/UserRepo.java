package ro.unibuc.car_messenger.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.unibuc.car_messenger.domain.User;

import java.util.Optional;

public interface UserRepo extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}
