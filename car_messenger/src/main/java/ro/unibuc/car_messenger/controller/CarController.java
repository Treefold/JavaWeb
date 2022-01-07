package ro.unibuc.car_messenger.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ro.unibuc.car_messenger.dto.CarDto;
import ro.unibuc.car_messenger.dto.UserDto;
import ro.unibuc.car_messenger.service.CarService;
import ro.unibuc.car_messenger.service.UserService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/car")
@RequiredArgsConstructor
@Validated
public class CarController {
    private final UserService userService;
    private final CarService carService;

    @GetMapping()
    public ResponseEntity<List<CarDto>> getCars(
            @RequestHeader(value = "login_username", required = false, defaultValue = "") String username,
            @RequestHeader(value = "login_password", required = false, defaultValue = "") String password
    ) {
        UserDto userDto = userService.handleLogin(username, password);
        try {
            userService.handleAdminLogin(username, password);
            return ResponseEntity.ok().body(carService.findAllCars());
        } catch (Exception e) {
            // filter to show only owned cars
            return ResponseEntity.ok().body(carService.findAllCars());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<CarDto> getCar(
            @RequestHeader(value = "login_username", required = false, defaultValue = "") String username,
            @RequestHeader(value = "login_password", required = false, defaultValue = "") String password,
            @PathVariable Long id
    ) {
        UserDto userDto = userService.handleLogin(username, password);
        Optional<CarDto> carDto = carService.findCarById(id);
        try {
            userService.handleAdminLogin(username, password);
            if (carDto.isEmpty()) { return ResponseEntity.notFound().build(); }
        } catch (Exception e) {
            if (carDto.isEmpty()) { return ResponseEntity.badRequest().build(); }
            // perform ownership check
        }
        return ResponseEntity.ok().body(carDto.get());
    }

    @PostMapping("/create")
    public ResponseEntity<CarDto> saveCar(
            @RequestHeader(value = "login_username", required = false, defaultValue = "") String username,
            @RequestHeader(value = "login_password", required = false, defaultValue = "") String password,
            @RequestBody CarDto carDto
    ) {
        UserDto userDto = userService.handleLogin(username, password);
        CarDto carDtoSaved = carService.saveCar(carDto);
        // add ownership of the car
        return ResponseEntity.created(null).body(carDtoSaved);
    }

    @PutMapping ("/update/{id}")
    public ResponseEntity<CarDto> updateCar(
            @RequestHeader(value = "login_username", required = false, defaultValue = "") String username,
            @RequestHeader(value = "login_password", required = false, defaultValue = "") String password,
            @PathVariable Long id,
            @RequestBody CarDto carDtoIn
    ) {
        UserDto userDto = userService.handleLogin(username, password);
        Optional<CarDto> carDto = carService.findCarById(id);
        try {
            userService.handleAdminLogin(username, password);
            if (carDto.isEmpty()) { return ResponseEntity.notFound().build(); }
        } catch (Exception e) {
            if (carDto.isEmpty()) { return ResponseEntity.badRequest().build(); }
            // perform ownership check
        }
        return ResponseEntity.ok().body(carService.updateCar(id, carDtoIn).get());
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<CarDto> deleteCar(
            @RequestHeader(value = "login_username", required = false, defaultValue = "") String username,
            @RequestHeader(value = "login_password", required = false, defaultValue = "") String password,
            @PathVariable Long id
    ) {
        UserDto userDto = userService.handleLogin(username, password);
        Optional<CarDto> carDto = carService.findCarById(id);
        try {
            userService.handleAdminLogin(username, password);
            if (carDto.isEmpty()) { return ResponseEntity.notFound().build(); }
        } catch (Exception e) {
            if (carDto.isEmpty()) { return ResponseEntity.badRequest().build(); }
            // perform ownership check
        }
        carService.deleteCar(id);
        return ResponseEntity.ok().body(carDto.get());
    }

}
