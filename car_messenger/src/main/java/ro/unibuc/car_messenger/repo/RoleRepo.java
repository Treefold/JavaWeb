package ro.unibuc.car_messenger.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ro.unibuc.car_messenger.domain.Role;
import ro.unibuc.car_messenger.domain.RoleType;

@Repository
public interface RoleRepo extends JpaRepository<Role, Long> {
    Role findByName(RoleType name);
}
