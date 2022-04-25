package ro.unibuc.car_messenger;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import ro.unibuc.car_messenger.domain.*;
import ro.unibuc.car_messenger.dto.CarDto;
import ro.unibuc.car_messenger.dto.EngineDto;
import ro.unibuc.car_messenger.dto.OwnershipDto;
import ro.unibuc.car_messenger.dto.UserDto;
import ro.unibuc.car_messenger.service.CarService;
import ro.unibuc.car_messenger.service.EngineService;
import ro.unibuc.car_messenger.service.OwnershipService;
import ro.unibuc.car_messenger.service.UserService;

@SpringBootApplication

public class CarMessengerApplication {

    public static void main(String[] args) { SpringApplication.run(CarMessengerApplication.class, args); }

    @Bean
    CommandLineRunner run(UserService userService, CarService carService, OwnershipService ownershipService, EngineService engineService) {
        return args -> {
            /*
            for (RoleType roleType : RoleType.values()) {
                userService.saveRole(new Role(null, roleType));
            }

            UserDto adminUserDto = userService.saveUser(new UserDto(null, "mihaidaniel@gmail.com", "Password0."));
            userService.addRoleToUser("mihaidaniel@gmail.com", RoleType.ADMIN);
            EngineDto engineDto = engineService.saveEngine(new EngineDto(null, "XYZ132", EngineType.DIESEL));
            CarDto adminCarDto = carService.saveCar(new CarDto(null, "Admin", "007", engineDto.getId()));
            OwnershipDto adminOwnershipDto = ownershipService.saveOwnership(new OwnershipDto(null, adminUserDto, adminCarDto, OwnershipType.OWNER));

            userService.saveUser(new UserDto(null, "Test1@gmail.com", "Test1."));
            //*/
        };
    }
}
