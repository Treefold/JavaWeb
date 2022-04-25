package ro.unibuc.car_messenger.controller;

import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import ro.unibuc.car_messenger.domain.OwnershipType;
import ro.unibuc.car_messenger.dto.CarDto;
import ro.unibuc.car_messenger.dto.EngineDto;
import ro.unibuc.car_messenger.dto.OwnershipDto;
import ro.unibuc.car_messenger.dto.UserDto;
import ro.unibuc.car_messenger.exception.UniqueException;
import ro.unibuc.car_messenger.models.CarView;
import ro.unibuc.car_messenger.service.CarService;
import ro.unibuc.car_messenger.service.EngineService;
import ro.unibuc.car_messenger.service.OwnershipService;
import ro.unibuc.car_messenger.service.UserService;

import java.util.Optional;

@RestController
@RequestMapping("/car")
@Validated
@Api(value = "/car",
        tags = "Cars")
public class CarController {
    @Autowired
    private UserService userService;
    @Autowired
    private CarService carService;
    @Autowired
    private OwnershipService ownershipService;
    @Autowired
    private EngineService engineService;

    @GetMapping("/{carId}")
    public ResponseEntity<CarView> getCar(
            @RequestHeader(value = "login_username", required = false, defaultValue = "") String username,
            @RequestHeader(value = "login_password", required = false, defaultValue = "") String password,
            @PathVariable Long carId
    ) {
        UserDto userDto = userService.handleLogin(username, password);
        Optional<CarDto> carDto = carService.findCarById(carId);
        if (carDto.isEmpty()) { return ResponseEntity.notFound().build(); }

        boolean isAuthorized;
        try {
            userService.handleAdminLogin(username, password);
            isAuthorized = true;
        } catch (Exception e) {
            Optional<OwnershipDto> ownershipDto = ownershipService.findFirstByUserIdAndCarId(userDto.getId(), carId);
            if (ownershipDto.isEmpty()) { return ResponseEntity.notFound().build(); }
            isAuthorized = ownershipDto.get().isAtLeastCoowner();
        }

        Optional<EngineDto> engineDtoOptional;
        if (carDto.get().getEngineId() != null) {
            engineDtoOptional = engineService.getEngine(carDto.get().getEngineId());
        } else {
            engineDtoOptional = Optional.empty();
        }

        CarView carView = new CarView(carDto.get(), engineDtoOptional);
        carView.addUsers(ownershipService.findAllByCarId(carDto.get().getId()), isAuthorized);

        return ResponseEntity.ok().body(carView);
    }

    @GetMapping("/view/{carId}")
    public ModelAndView getCarView(@PathVariable Long carId) {
        ModelAndView model = new ModelAndView("car-view");

        Authentication userAuth = SecurityContextHolder.getContext().getAuthentication();
        if (userAuth instanceof AnonymousAuthenticationToken) {
            model.setViewName("redirect:/login");
            return model;
        }

        UserDto userDto = userService.getUser(userAuth.getName()).get();

        Optional<CarDto> carDto = carService.findCarById(carId);
        if (carDto.isEmpty()) {
            model.setViewName("redirect:/notfound");
            return model;
        }

        boolean isAuthorized = false;
        if (userAuth.getAuthorities().contains("ADMIN")) {
            isAuthorized = true;
        } else {
            Optional<OwnershipDto> ownershipDto = ownershipService.findFirstByUserIdAndCarId(userDto.getId(), carId);
            if (ownershipDto.isEmpty()){
                model.setViewName("redirect:/notfound");
                return model;
            }
            isAuthorized = ownershipDto.get().isAtLeastCoowner();
        }

        Optional<EngineDto> engineDtoOptional;
        if (carDto.get().getEngineId() != null) {
            engineDtoOptional = engineService.getEngine(carDto.get().getEngineId());
        } else {
            engineDtoOptional = Optional.empty();
        }

        CarView carView = new CarView(carDto.get(), engineDtoOptional);
        carView.addUsers(ownershipService.findAllByCarId(carDto.get().getId()), isAuthorized);

        model.addObject("carView", carView);
        model.addObject("ownerUsername", userService.getUser(carView.getOwnerUserId()).get().getUsername());
        model.addObject("coownersUsernames", userService.getUsers(carView.getCoownerUserIds()).stream().map(u -> u.getUsername()).toList());;
        model.addObject("pendingInvitations", userService.getUsers(carView.getPendingInvitationUserIds()).stream().map(u -> u.getUsername()).toList());;
        model.addObject("pendingRequests", userService.getUsers(carView.getPendingRequestUserIds()).stream().map(u -> u.getUsername()).toList());

        return model;
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
    public String deleteCar(@PathVariable Long carId) {
        Authentication userAuth = SecurityContextHolder.getContext().getAuthentication();
        UserDto userDto = userService.getUser(userAuth.getName()).get();
        Optional<CarDto> carDto = carService.findCarById(carId);
        if (carDto.isEmpty()) { return "notfound"; }

        if (!userAuth.getAuthorities().contains("ADMIN")) {
            Optional<OwnershipDto> ownershipDto = ownershipService.findFirstByUserIdAndCarId(userDto.getId(), carId);
            if (ownershipDto.isEmpty() || !ownershipDto.get().isAtLeastCoowner()) {
                return "notfound";
            }
            if (ownershipDto.get().isOwner()) { carService.deleteCar(carId); }
            else { ownershipService.deleteOwnership(ownershipDto.get().getId()); }
        }

        return "home";
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

    @PostMapping("/invite/{carId}/{invitedUserUsername}")
    public ResponseEntity<Void> inviteCoowner(
            @RequestHeader(value = "login_username", required = false, defaultValue = "") String username,
            @RequestHeader(value = "login_password", required = false, defaultValue = "") String password,
            @PathVariable Long carId,
            @PathVariable String invitedUserUsername
    ) {
        UserDto userDto = userService.handleLogin(username, password);
        Optional<CarDto> carDto = carService.findCarById(carId);
        if (carDto.isEmpty()) { return ResponseEntity.notFound().build(); }
        Optional<OwnershipDto> ownershipDto = ownershipService.findFirstByUserIdAndCarId(userDto.getId(), carId);
        if (ownershipDto.isEmpty() || !ownershipDto.get().isOwner()) {
            return ResponseEntity.notFound().build();
        }
        Optional<UserDto> invitedUserDto = userService.getUser(invitedUserUsername);
        if (invitedUserDto.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        if (ownershipService.findFirstByUserIdAndCarId(invitedUserDto.get().getId(), carId).isPresent()) {
            throw new UniqueException("There already exists a relationship between the invited user and the car");
        }

        ownershipService.saveOwnership(new OwnershipDto(null, invitedUserDto.get(), carDto.get(), OwnershipType.INVITED));
        return ResponseEntity.created(null).build();
    }

    @PutMapping("/invite/{carId}")
    public ResponseEntity<Void> acceptInviteCoownership(
            @RequestHeader(value = "login_username", required = false, defaultValue = "") String username,
            @RequestHeader(value = "login_password", required = false, defaultValue = "") String password,
            @PathVariable Long carId
    ) {
        UserDto invitedUserDto = userService.handleLogin(username, password);
        Optional<CarDto> carDto = carService.findCarById(carId);
        if (carDto.isEmpty()) { return ResponseEntity.notFound().build(); }

        Optional<OwnershipDto> invitedOwnershipDto = ownershipService.findFirstByUserIdAndCarId(invitedUserDto.getId(), carId);
        if (invitedOwnershipDto.isEmpty() || !invitedOwnershipDto.get().isInvited()) {
            return ResponseEntity.notFound().build();
        }

        ownershipService.updateOwnership(invitedOwnershipDto.get().getId(), OwnershipType.COOWNER);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/invite/{carId}/{invitedUserUsername}")
    public ResponseEntity<Void> acceptInviteCoownershipByAdmin(
            @RequestHeader(value = "login_username", required = false, defaultValue = "") String username,
            @RequestHeader(value = "login_password", required = false, defaultValue = "") String password,
            @PathVariable Long carId,
            @PathVariable String invitedUserUsername
    ) {
        userService.handleAdminLogin(username, password);
        Optional<CarDto> carDto = carService.findCarById(carId);
        if (carDto.isEmpty()) { return ResponseEntity.notFound().build(); }
        Optional<UserDto> invitedUserDto = userService.getUser(invitedUserUsername);
        if (invitedUserDto.isEmpty()) { return ResponseEntity.notFound().build(); }
        Optional<OwnershipDto> invitedOwnershipDto = ownershipService.findFirstByUserIdAndCarId(invitedUserDto.get().getId(), carId);
        if (invitedOwnershipDto.isEmpty() || !invitedOwnershipDto.get().isInvited()) {
            return ResponseEntity.notFound().build();
        }

        ownershipService.updateOwnership(invitedOwnershipDto.get().getId(), OwnershipType.COOWNER);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/invite/{carId}")
    public ResponseEntity<Void> declineInviteCoownership(
            @RequestHeader(value = "login_username", required = false, defaultValue = "") String username,
            @RequestHeader(value = "login_password", required = false, defaultValue = "") String password,
            @PathVariable Long carId
    ) {
        UserDto invitedUserDto = userService.handleLogin(username, password);
        Optional<CarDto> carDto = carService.findCarById(carId);
        if (carDto.isEmpty()) { return ResponseEntity.notFound().build(); }
        Optional<OwnershipDto> inviteOwnershipDto = ownershipService.findFirstByUserIdAndCarId(invitedUserDto.getId(), carId);
        if (inviteOwnershipDto.isEmpty() || !inviteOwnershipDto.get().isInvited()) {
            return ResponseEntity.notFound().build();
        }

        ownershipService.deleteOwnership(inviteOwnershipDto.get().getId());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/invite/{carId}/{requesterUserUsername}")
    public ResponseEntity<Void> deleteInviteCoownership(
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

        Optional<UserDto> invitedUserDto = userService.getUser(requesterUserUsername);
        if (invitedUserDto.isEmpty()) { return ResponseEntity.notFound().build(); }
        Optional<OwnershipDto> inviteOwnershipDto = ownershipService.findFirstByUserIdAndCarId(invitedUserDto.get().getId(), carId);
        if (inviteOwnershipDto.isEmpty() || !inviteOwnershipDto.get().isInvited()) {
            return ResponseEntity.notFound().build();
        }

        ownershipService.deleteOwnership(inviteOwnershipDto.get().getId());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/remove/{carId}/{removedUserUsername}")
    public ResponseEntity<Void> removeOwnership(
            @RequestHeader(value = "login_username", required = false, defaultValue = "") String username,
            @RequestHeader(value = "login_password", required = false, defaultValue = "") String password,
            @PathVariable Long carId,
            @PathVariable String removedUserUsername
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

        Optional<UserDto> removedUserDto = userService.getUser(removedUserUsername);
        if (removedUserDto.isEmpty()) { return ResponseEntity.notFound().build(); }
        Optional<OwnershipDto> removedOwnershipDto = ownershipService.findFirstByUserIdAndCarId(removedUserDto.get().getId(), carId);
        if (removedOwnershipDto.isEmpty()) { return ResponseEntity.notFound().build(); }
        if (removedOwnershipDto.get().isOwner()) { return ResponseEntity.badRequest().build(); }

        ownershipService.deleteOwnership(removedOwnershipDto.get().getId());
        return ResponseEntity.ok().build();
    }

}
