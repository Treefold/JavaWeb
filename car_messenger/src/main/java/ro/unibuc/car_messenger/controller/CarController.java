package ro.unibuc.car_messenger.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ro.unibuc.car_messenger.domain.OwnershipType;
import ro.unibuc.car_messenger.dto.CarDto;
import ro.unibuc.car_messenger.dto.OwnershipDto;
import ro.unibuc.car_messenger.dto.UserDto;
import ro.unibuc.car_messenger.exception.UniqueException;
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

    @GetMapping("/{carId}")
    public ResponseEntity<CarView> getCar(
            @RequestHeader(value = "login_username", required = false, defaultValue = "") String username,
            @RequestHeader(value = "login_password", required = false, defaultValue = "") String password,
            @PathVariable Long carId
    ) {
        UserDto userDto = userService.handleLogin(username, password);
        Optional<CarDto> carDto = carService.findCarById(carId);
        if (carDto.isEmpty()) { return ResponseEntity.notFound().build(); }

        boolean isAuthorized = false;
        try {
            userService.handleAdminLogin(username, password);
            isAuthorized = true;
        } catch (Exception e) {
            Optional<OwnershipDto> ownershipDto = ownershipDto = ownershipService.findFirstByUserIdAndCarId(userDto.getId(), carId);
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

    @PutMapping ("/update/{carId}")
    public ResponseEntity<CarDto> updateCar(
            @RequestHeader(value = "login_username", required = false, defaultValue = "") String username,
            @RequestHeader(value = "login_password", required = false, defaultValue = "") String password,
            @PathVariable Long carId,
            @RequestBody CarDto carDtoIn
    ) {
        UserDto userDto = userService.handleLogin(username, password);
        Optional<CarDto> carDto = carService.findCarById(carId);
        if (carDto.isEmpty()) { return ResponseEntity.notFound().build(); }
        try {
            userService.handleAdminLogin(username, password);
        } catch (Exception e) {
            Optional<OwnershipDto> ownershipDto = ownershipService.findFirstByUserIdAndCarId(userDto.getId(), carId);
            if (ownershipDto.isEmpty() || !ownershipDto.get().isAtLeastCoowner()) {
                return ResponseEntity.notFound().build();
            }
        }
        return ResponseEntity.ok().body(carService.updateCar(carId, carDtoIn).get());
    }

    @DeleteMapping("/delete/{carId}")
    public ResponseEntity<CarDto> deleteCar(
            @RequestHeader(value = "login_username", required = false, defaultValue = "") String username,
            @RequestHeader(value = "login_password", required = false, defaultValue = "") String password,
            @PathVariable Long carId
    ) {
        UserDto userDto = userService.handleLogin(username, password);
        Optional<CarDto> carDto = carService.findCarById(carId);
        if (carDto.isEmpty()) { return ResponseEntity.notFound().build(); }
        try {
            userService.handleAdminLogin(username, password);
        } catch (Exception e) {
            Optional<OwnershipDto> ownershipDto = ownershipService.findFirstByUserIdAndCarId(userDto.getId(), carId);
            if (ownershipDto.isEmpty() || !ownershipDto.get().isOwner()) {
                return ResponseEntity.notFound().build();
            }
        }
        carService.deleteCar(carId);
        return ResponseEntity.ok().body(carDto.get());
    }

    @PostMapping("/request/{carId}")
    public ResponseEntity<Void> requestCoownership(
            @RequestHeader(value = "login_username", required = false, defaultValue = "") String username,
            @RequestHeader(value = "login_password", required = false, defaultValue = "") String password,
            @PathVariable Long carId
    ) {
        UserDto userDto = userService.handleLogin(username, password);
        Optional<CarDto> carDto = carService.findCarById(carId);
        if (carDto.isEmpty()) { return ResponseEntity.notFound().build(); }
        if (ownershipService.findFirstByUserIdAndCarId(userDto.getId(), carId).isPresent()) {
            throw new UniqueException("There already exists a relationship between you and the car");
        }

        ownershipService.saveOwnership(new OwnershipDto(null, userDto, carDto.get(), OwnershipType.REQUESTED));
        return ResponseEntity.created(null).build();
    }


    @PutMapping("/request/{carId}/{requesterUserUsername}")
    public ResponseEntity<Void> acceptRequestCoownership(
            @RequestHeader(value = "login_username", required = false, defaultValue = "") String username,
            @RequestHeader(value = "login_password", required = false, defaultValue = "") String password,
            @PathVariable Long carId,
            @PathVariable String requesterUserUsername
    ) {
        UserDto userDto = userService.handleLogin(username, password);
        Optional<CarDto> carDto = carService.findCarById(carId);
        if (carDto.isEmpty()) { return ResponseEntity.notFound().build(); }

        try {
            userService.handleAdminLogin(username, password);
        } catch (Exception e) {
            Optional<OwnershipDto> ownerOwnershipDto  = ownershipService.findFirstByUserIdAndCarId(userDto.getId(), carId);
            if (ownerOwnershipDto.isEmpty() || !ownerOwnershipDto.get().isOwner()) { return ResponseEntity.notFound().build(); }
        }

        Optional<UserDto> requesterUserDto = userService.getUser(requesterUserUsername);
        if (requesterUserDto.isEmpty()) { return ResponseEntity.notFound().build(); }
        Optional<OwnershipDto> requesterOwnershipDto = ownershipService.findFirstByUserIdAndCarId(requesterUserDto.get().getId(), carId);
        if (requesterOwnershipDto.isEmpty() || !requesterOwnershipDto.get().isRequested()) {
            return ResponseEntity.notFound().build();
        }

        ownershipService.updateOwnership(requesterOwnershipDto.get().getId(), OwnershipType.COOWNER);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/request/{carId}")
    public ResponseEntity<Void> deleteRequestCoownership(
            @RequestHeader(value = "login_username", required = false, defaultValue = "") String username,
            @RequestHeader(value = "login_password", required = false, defaultValue = "") String password,
            @PathVariable Long carId
    ) {
        UserDto userDto = userService.handleLogin(username, password);
        Optional<CarDto> carDto = carService.findCarById(carId);
        if (carDto.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Optional<OwnershipDto> requesterOwnershipDto = ownershipService.findFirstByUserIdAndCarId(userDto.getId(), carId);
        if (requesterOwnershipDto.isEmpty() || !requesterOwnershipDto.get().isRequested()) {
            return ResponseEntity.notFound().build();
        }

        ownershipService.deleteOwnership(requesterOwnershipDto.get().getId());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/request/{carId}/{requesterUserUsername}")
    public ResponseEntity<Void> declineRequestCoownership(
            @RequestHeader(value = "login_username", required = false, defaultValue = "") String username,
            @RequestHeader(value = "login_password", required = false, defaultValue = "") String password,
            @PathVariable Long carId,
            @PathVariable String requesterUserUsername
    ) {
        UserDto userDto = userService.handleLogin(username, password);
        Optional<CarDto> carDto = carService.findCarById(carId);
        if (carDto.isEmpty()) { return ResponseEntity.notFound().build(); }

        try {
            userService.handleAdminLogin(username, password);
        } catch (Exception e) {
            Optional<OwnershipDto> ownerOwnershipDto  = ownershipService.findFirstByUserIdAndCarId(userDto.getId(), carId);
            if (ownerOwnershipDto.isEmpty() || !ownerOwnershipDto.get().isOwner()) { return ResponseEntity.notFound().build(); }
        }

        Optional<UserDto> requesterUserDto = userService.getUser(requesterUserUsername);
        if (requesterUserDto.isEmpty()) { return ResponseEntity.notFound().build(); }
        Optional<OwnershipDto> requesterOwnershipDto = ownershipService.findFirstByUserIdAndCarId(requesterUserDto.get().getId(), carId);
        if (requesterOwnershipDto.isEmpty() || !requesterOwnershipDto.get().isRequested()) {
            return ResponseEntity.notFound().build();
        }

        ownershipService.deleteOwnership(requesterOwnershipDto.get().getId());
        return ResponseEntity.ok().build();
    }

}
