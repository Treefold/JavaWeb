package ro.unibuc.car_messenger.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ro.unibuc.car_messenger.domain.Car;
import ro.unibuc.car_messenger.domain.Ownership;
import ro.unibuc.car_messenger.domain.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface OwnershipRepo extends JpaRepository<Ownership, Long> {

    List<Ownership> findAllByUserId (Long userId);
    List<Ownership> findAllByCarId  (Long carId);
    Optional<Ownership> findFirstByUserIdAndCarId (Long userId, Long carId);
    Optional<Ownership> findFirstByUserAndCar (User user, Car car);


}
