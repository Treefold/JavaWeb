package ro.unibuc.car_messenger;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import ro.unibuc.car_messenger.domain.Role;
import ro.unibuc.car_messenger.domain.RoleType;
import ro.unibuc.car_messenger.domain.User;
import ro.unibuc.car_messenger.service.UserService;

import java.util.ArrayList;

import static ro.unibuc.car_messenger.domain.RoleType.ADMIN;

@SpringBootApplication
public class CarMessengerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CarMessengerApplication.class, args);
    }

    @Bean
    CommandLineRunner run(UserService userService) {
        return args -> {
            for (RoleType roleType : RoleType.values()) {
                userService.saveRole(new Role(null, roleType));
            }
            userService.saveUser(new User(null, "mihaidaniel", "Password0.", new ArrayList<>()));
            userService.addRoleToUser("mihaidaniel", ADMIN);
        };
    }
}
