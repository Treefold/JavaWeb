package ro.unibuc.car_messenger;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import ro.unibuc.car_messenger.domain.Role;
import ro.unibuc.car_messenger.domain.RoleType;
import ro.unibuc.car_messenger.dto.CarDto;
import ro.unibuc.car_messenger.dto.UserDto;
import ro.unibuc.car_messenger.service.CarService;
import ro.unibuc.car_messenger.service.UserService;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import static ro.unibuc.car_messenger.domain.RoleType.ADMIN;

@SpringBootApplication
@EnableSwagger2

public class CarMessengerApplication {

    public static void main(String[] args) { SpringApplication.run(CarMessengerApplication.class, args); }

    @Bean
    CommandLineRunner run(UserService userService, CarService carService) {
        return args -> {
            for (RoleType roleType : RoleType.values()) {
                userService.saveRole(new Role(null, roleType));
            }
            userService.saveUser(new UserDto(null, "mihaidaniel@gmail.com", "Password0."));
            userService.addRoleToUser("mihaidaniel@gmail.com", ADMIN);

            CarDto carDto = carService.saveCar(new CarDto(null, "Admin", "007"));
            System.out.println(carService.findCarByPlateAndCountryCode(carDto.getPlate(), carDto.getCountryCode()));
            carService.updateCar(carDto.getId(), new CarDto(null, "NoAdmin", "13"));
            System.out.println(carService.findCarById(carDto.getId()));
            carService.deleteCar(carDto.getId());
            System.out.println(carService.findCarById(carDto.getId()));
        };
    }
}
