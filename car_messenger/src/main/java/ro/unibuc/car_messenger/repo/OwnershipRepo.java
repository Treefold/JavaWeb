package ro.unibuc.car_messenger.repo;

import net.bytebuddy.TypeCache;
import org.springframework.data.domain.Sort;
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
    List<Ownership> findAllByUserId (Long userId, Sort sort);
    List<Ownership> findAllByCarId  (Long carId);
    Optional<Ownership> findFirstByUserIdAndCarId (Long userId, Long carId);
    Optional<Ownership> findFirstByUserAndCar (User user, Car car);

}
