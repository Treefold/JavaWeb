package ro.unibuc.car_messenger.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ro.unibuc.car_messenger.domain.OwnershipType;
import ro.unibuc.car_messenger.dto.CarDto;
import ro.unibuc.car_messenger.dto.OwnershipDto;
import ro.unibuc.car_messenger.dto.UserDto;
import ro.unibuc.car_messenger.models.CarView;
import ro.unibuc.car_messenger.service.CarService;
import ro.unibuc.car_messenger.service.OwnershipService;
import ro.unibuc.car_messenger.service.UserService;

import java.util.Optional;

@RestController
@RequestMapping("/car")
@RequiredArgsConstructor
@Validated
public class CarController {
    private final UserService userService;
    private final CarService carService;
    private final OwnershipService ownershipService;

    @GetMapping("/{id}")
    public ResponseEntity<CarView> getCar(
            @RequestHeader(value = "login_username", required = false, defaultValue = "") String username,
            @RequestHeader(value = "login_password", required = false, defaultValue = "") String password,
            @PathVariable Long id
    ) {
        UserDto userDto = userService.handleLogin(username, password);
        Optional<CarDto> carDto = carService.findCarById(id);
        if (carDto.isEmpty()) { return ResponseEntity.notFound().build(); }

        boolean isAuthorized = false;
        try {
            userService.handleAdminLogin(username, password);
            isAuthorized = true;
        } catch (Exception e) {
            Optional<OwnershipDto> ownershipDto = ownershipDto = ownershipService.findFirstByUserIdAndCarId(userDto.getId(), carDto.get().getId());
            if (ownershipDto.isEmpty()) { return ResponseEntity.notFound().build(); }
            isAuthorized = ownershipDto.get().isOwner();
        }
        CarView carView = new CarView(carDto.get());
        carView.addUsers(ownershipService.findAllByCarId(carDto.get().getId()), isAuthorized);

        return ResponseEntity.ok().body(carView);
    }

    @PostMapping("/create")
    public ResponseEntity<CarDto> saveCar(
            @RequestHeader(value = "login_username", required = false, defaultValue = "") String username,
            @RequestHeader(value = "login_password", required = false, defaultValue = "") String password,
            @RequestBody CarDto carDto
    ) {
        UserDto userDto = userService.handleLogin(username, password);
        CarDto savedCarDto = carService.saveCar(carDto);
        ownershipService.saveOwnership(new OwnershipDto(null, userDto, savedCarDto, OwnershipType.OWNER));
        return ResponseEntity.created(null).body(savedCarDto);
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
        if (carDto.isEmpty()) { return ResponseEntity.notFound().build(); }
        try {
            userService.handleAdminLogin(username, password);
        } catch (Exception e) {
            Optional<OwnershipDto> ownershipDto = ownershipService.findFirstByUserIdAndCarId(userDto.getId(), carDto.get().getId());
            if (ownershipDto.isEmpty() || !ownershipDto.get().isAtLeastCoowner()) {
                return ResponseEntity.notFound().build();
            }
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
        if (carDto.isEmpty()) { return ResponseEntity.notFound().build(); }
        try {
            userService.handleAdminLogin(username, password);
        } catch (Exception e) {
            Optional<OwnershipDto> ownershipDto = ownershipService.findFirstByUserIdAndCarId(userDto.getId(), carDto.get().getId());
            if (ownershipDto.isEmpty() || !ownershipDto.get().isOwner()) {
                return ResponseEntity.notFound().build();
            }
        }
        carService.deleteCar(id);
        return ResponseEntity.ok().body(carDto.get());
    }

}
