package ro.unibuc.car_messenger.controller;

import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ro.unibuc.car_messenger.domain.EngineType;
import ro.unibuc.car_messenger.domain.OwnershipType;
import ro.unibuc.car_messenger.dto.CarDto;
import ro.unibuc.car_messenger.dto.EngineDto;
import ro.unibuc.car_messenger.dto.OwnershipDto;
import ro.unibuc.car_messenger.dto.UserDto;
import ro.unibuc.car_messenger.exception.UniqueException;
import ro.unibuc.car_messenger.models.CarView;
import ro.unibuc.car_messenger.models.InviteCoowner;
import ro.unibuc.car_messenger.service.CarService;
import ro.unibuc.car_messenger.service.EngineService;
import ro.unibuc.car_messenger.service.OwnershipService;
import ro.unibuc.car_messenger.service.UserService;

import javax.validation.Valid;
import java.util.Arrays;
import java.util.Optional;

@Controller
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

    @GetMapping("/view/{carId}")
    public String getCarView(@PathVariable Long carId, Model model) {
        Authentication userAuth = SecurityContextHolder.getContext().getAuthentication();
        if (userAuth instanceof AnonymousAuthenticationToken) { return "redirect:/login"; }
        UserDto userDto = userService.getUser(userAuth.getName()).get();

        Optional<CarDto> carDto = carService.findCarById(carId);
        if (carDto.isEmpty()) { return "redirect:/notfound";  }

        boolean isAuthorized = false;
        if (userAuth.getAuthorities().contains("ADMIN")) {
            isAuthorized = true;
        } else {
            Optional<OwnershipDto> ownershipDto = ownershipService.findFirstByUserIdAndCarId(userDto.getId(), carId);
            if (ownershipDto.isEmpty() || !ownershipDto.get().isAtLeastCoowner()) { return "redirect:/notfound"; }
            isAuthorized = ownershipDto.get().isOwner();
        }

        Optional<EngineDto> engineDtoOptional;
        if (carDto.get().getEngineId() != null) {
            engineDtoOptional = engineService.getEngine(carDto.get().getEngineId());
        } else {
            engineDtoOptional = Optional.empty();
        }

        CarView carView = new CarView(carDto.get(), engineDtoOptional);
        carView.addUsers(ownershipService.findAllByCarId(carDto.get().getId()), isAuthorized);

        model.addAttribute("isAuthorized", isAuthorized);
        model.addAttribute("carView", carView);
        model.addAttribute("ownerUsername", userService.getUser(carView.getOwnerUserId()).get().getUsername());
        model.addAttribute("coownersUsernames", userService.getUsers(carView.getCoownerUserIds()).stream().map(u -> u.getUsername()).toList());
        if(isAuthorized) {
            model.addAttribute("pendingInvitations", userService.getUsers(carView.getPendingInvitationUserIds()).stream().map(u -> u.getUsername()).toList());
            model.addAttribute("pendingRequests", userService.getUsers(carView.getPendingRequestUserIds()).stream().map(u -> u.getUsername()).toList());
        }

        return "car-view";
    }

    @GetMapping("/create")
    public String newCarForm(Model model) {
        Authentication userAuth = SecurityContextHolder.getContext().getAuthentication();
        if (userAuth instanceof AnonymousAuthenticationToken) {
            return "redirect:/login";
        }

        model.addAttribute("carDto", new CarDto());
        model.addAttribute("engineDto", new EngineDto());
        model.addAttribute("engineTypes", Arrays.stream(EngineType.values()).toList());
        return "car-create";
    }

    @PostMapping("/create")
    public String saveCar(
            @ModelAttribute @Valid CarDto carDto,
            @ModelAttribute @Valid EngineDto engineDto,
            BindingResult bindingResult,
            Model model
    ) {
        Authentication userAuth = SecurityContextHolder.getContext().getAuthentication();
        if (userAuth instanceof AnonymousAuthenticationToken) {
            return "redirect:/login";
        }
        UserDto userDto = userService.getUser(userAuth.getName()).get();

        if (bindingResult.hasErrors()) {
            return "car-create";
        }

        EngineDto savedEngineDto = engineService.saveEngine(engineDto);
        carDto.setEngineId(savedEngineDto.getId());
        CarDto savedCarDto = carService.saveCar(carDto);
        ownershipService.saveOwnership(new OwnershipDto(null, userDto, savedCarDto, OwnershipType.OWNER));
        return "redirect:/car/view/" + savedCarDto.getId();
    }

    @GetMapping("/update/{carId}")
    public String updateCarForm(@PathVariable Long carId, Model model) {
        Authentication userAuth = SecurityContextHolder.getContext().getAuthentication();
        if (userAuth instanceof AnonymousAuthenticationToken) { return "redirect:/login"; }
        UserDto userDto = userService.getUser(userAuth.getName()).get();

        Optional<CarDto> carDto = carService.findCarById(carId);
        if (carDto.isEmpty()) { return "redirect:/notfound"; }

        boolean isAuthorized = false;
        if (userAuth.getAuthorities().contains("ADMIN")) {
            isAuthorized = true;
        } else {
            Optional<OwnershipDto> ownershipDto = ownershipService.findFirstByUserIdAndCarId(userDto.getId(), carId);
            if (ownershipDto.isPresent() && ownershipDto.get().isOwner()) {
                isAuthorized = true;
            }
        }
        if (!isAuthorized) { return "redirect:/notfound"; }

        EngineDto engineDto = engineService.getEngine(carDto.get().getEngineId()).get();

        model.addAttribute("carDto", carDto.get());
        model.addAttribute("engineDto", engineDto);
        model.addAttribute("engineTypes", Arrays.stream(EngineType.values()).toList());
        return "car-update";
    }

    @PostMapping ("/update/{carId}")
    public String updateCar(
            @PathVariable Long carId,
            @Valid CarDto carDtoForm,
            @Valid EngineDto engineDtoForm,
            BindingResult result) {
        Authentication userAuth = SecurityContextHolder.getContext().getAuthentication();
        if (userAuth instanceof AnonymousAuthenticationToken) { return "redirect:/login"; }
        UserDto userDto = userService.getUser(userAuth.getName()).get();

        Optional<CarDto> carDto = carService.findCarById(carId);
        if (carDto.isEmpty()) { return "redirect:/notfound"; }

        boolean isAuthorized = false;
        if (userAuth.getAuthorities().contains("ADMIN")) {
            isAuthorized = true;
        } else {
            Optional<OwnershipDto> ownershipDto = ownershipService.findFirstByUserIdAndCarId(userDto.getId(), carId);
            if (ownershipDto.isPresent() && ownershipDto.get().isOwner()) {
                isAuthorized = true;
            }
        }
        if (!isAuthorized) { return "redirect:/notfound"; }

        if (result.hasErrors()) {
            return "car-update";
        }

        engineService.updateEngine(carDto.get().getEngineId(), engineDtoForm);
        carService.updateCar(carId, carDtoForm);

        return "redirect:/car/view/" + carId;
    }

    @GetMapping("/delete/{carId}")
    public String deleteCar(@PathVariable Long carId) {
        Authentication userAuth = SecurityContextHolder.getContext().getAuthentication();
        if (userAuth instanceof AnonymousAuthenticationToken) { return "redirect:/login"; }
        UserDto userDto = userService.getUser(userAuth.getName()).get();

        Optional<CarDto> carDto = carService.findCarById(carId);
        if (carDto.isEmpty()) { return "redirect:/notfound"; }

        if (!userAuth.getAuthorities().contains("ADMIN")) {
            Optional<OwnershipDto> ownershipDto = ownershipService.findFirstByUserIdAndCarId(userDto.getId(), carId);
            if (ownershipDto.isEmpty() || !ownershipDto.get().isAtLeastCoowner()) {
                return "redirect:/notfound";
            }
            if (ownershipDto.get().isOwner()) { carService.deleteCar(carId); }
            else { ownershipService.deleteOwnership(ownershipDto.get().getId()); }
        }

        return "redirect:/";
    }

    @GetMapping("/invite/{carId}")
    public String inviteCoowner(@PathVariable Long carId, Model model) {
        Authentication userAuth = SecurityContextHolder.getContext().getAuthentication();
        if (userAuth instanceof AnonymousAuthenticationToken) { return "redirect:/login"; }
        UserDto userDto = userService.getUser(userAuth.getName()).get();

        Optional<CarDto> carDto = carService.findCarById(carId);
        if (carDto.isEmpty()) { return "redirect:/notfound"; }
        Optional<OwnershipDto> ownershipDto = ownershipService.findFirstByUserIdAndCarId(userDto.getId(), carId);
        if (ownershipDto.isEmpty() || !ownershipDto.get().isOwner()) { return "redirect:/notfound"; }

        model.addAttribute("inviteCoowner", new InviteCoowner());

        return "invite";
    }

    @PostMapping("/invite/{carId}")
    public String inviteCoowner(
            @PathVariable Long carId,
            @Valid InviteCoowner inviteCoowner,
            BindingResult result,
            Model model
    ) {
        Authentication userAuth = SecurityContextHolder.getContext().getAuthentication();
        if (userAuth instanceof AnonymousAuthenticationToken) { return "redirect:/login"; }
        UserDto userDto = userService.getUser(userAuth.getName()).get();

        Optional<CarDto> carDto = carService.findCarById(carId);
        if (carDto.isEmpty()) { return "redirect:/notfound"; }
        Optional<OwnershipDto> ownershipDto = ownershipService.findFirstByUserIdAndCarId(userDto.getId(), carId);
        if (ownershipDto.isEmpty() || !ownershipDto.get().isOwner()) { return "redirect:/notfound"; }

        if (result.hasErrors()) {
            return "invite";
        }

        Optional<UserDto> invitedUserDto = userService.getUser(inviteCoowner.getUsername());
        if (invitedUserDto.isEmpty()) {
            // add user not found error
            return "invite";
        }
        if (ownershipService.findFirstByUserIdAndCarId(invitedUserDto.get().getId(), carId).isPresent()) {
            throw new UniqueException("There already exists a relationship between the invited user and the car");
        }

        ownershipService.saveOwnership(new OwnershipDto(null, invitedUserDto.get(), carDto.get(), OwnershipType.INVITED));
        return "redirect:/car/view/" + carId;
    }

    @GetMapping("/remove/{carId}/{removedUserUsername}")
    public String removeOwnership(
            @PathVariable Long carId,
            @PathVariable String removedUserUsername
    ) {
        Authentication userAuth = SecurityContextHolder.getContext().getAuthentication();
        if (userAuth instanceof AnonymousAuthenticationToken) {
            return "redirect:/login";
        }
        UserDto userDto = userService.getUser(userAuth.getName()).get();

        Optional<CarDto> carDto = carService.findCarById(carId);
        if (carDto.isEmpty()) {
            return "redirect:/";
        }

        Optional<UserDto> removedUserDto = userService.getUser(removedUserUsername);
        if (removedUserDto.isEmpty()) {
            return "redirect:/";
        }

        String finalRedirect = "redirect:/car/view/" + carId;
        boolean isAuthorized = false;
        if (userAuth.getAuthorities().contains("ADMIN")) {
            isAuthorized = true;
        } else if (userDto.getId() == removedUserDto.get().getId()){
            isAuthorized = true;
            finalRedirect = "redirect:/";
        } else {
            Optional<OwnershipDto> ownershipDto = ownershipService.findFirstByUserIdAndCarId(userDto.getId(), carId);
            if (ownershipDto.isPresent() && ownershipDto.get().isOwner()) {
                isAuthorized = true;
            }
        }
        if (!isAuthorized) { return "redirect:/"; }
        Optional<OwnershipDto> removedOwnershipDto = ownershipService.findFirstByUserIdAndCarId(removedUserDto.get().getId(), carId);
        if (removedOwnershipDto.isPresent() && !removedOwnershipDto.get().isOwner()) { ownershipService.deleteOwnership(removedOwnershipDto.get().getId()); }

        return finalRedirect;
    }

    @GetMapping("/remove/{carId}")
    public String removeOwnOwnership(@PathVariable Long carId) {
        Authentication userAuth = SecurityContextHolder.getContext().getAuthentication();
        if (userAuth instanceof AnonymousAuthenticationToken) { return "redirect:/login"; }
        return "redirect:" + carId + "/" + userAuth.getName();
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
}
