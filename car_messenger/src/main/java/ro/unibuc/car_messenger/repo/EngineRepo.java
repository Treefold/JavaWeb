package ro.unibuc.car_messenger.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ro.unibuc.car_messenger.domain.Engine;
import ro.unibuc.car_messenger.domain.EngineType;

import java.util.List;
import java.util.Optional;

@Repository
public interface EngineRepo extends JpaRepository<Engine, Long> {
    Optional<Engine> findFirstByNumber (String number);
    List<Engine> findByType (EngineType type);
}
