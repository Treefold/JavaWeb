package ro.unibuc.car_messenger.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.unibuc.car_messenger.domain.Car;

import java.util.Optional;

public interface CarRepo extends JpaRepository<Car, Long> {
    Optional<Car> findByPlateAndCountryCode (String plate, String countryCode);
}
