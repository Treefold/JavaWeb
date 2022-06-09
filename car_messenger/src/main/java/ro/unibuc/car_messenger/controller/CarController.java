package ro.unibuc.car_messenger.controller;

import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
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
import ro.unibuc.car_messenger.models.RequestCoownership;
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

        if (result.hasErrors()) { return "car-update"; }

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
        if (carDto.isEmpty()) { return "redirect:/"; }

        if (!userAuth.getAuthorities().contains("ADMIN")) {
            Optional<OwnershipDto> ownershipDto = ownershipService.findFirstByUserIdAndCarId(userDto.getId(), carId);
            if (ownershipDto.isEmpty() || !ownershipDto.get().isAtLeastCoowner()) { return "redirect:/"; }
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
        if (carDto.isEmpty()) { return "redirect:/"; }
        Optional<OwnershipDto> ownershipDto = ownershipService.findFirstByUserIdAndCarId(userDto.getId(), carId);
        if (ownershipDto.isEmpty() || !ownershipDto.get().isOwner()) { return "redirect:/"; }

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
        if (carDto.isEmpty()) { return "redirect:/"; }
        Optional<OwnershipDto> ownershipDto = ownershipService.findFirstByUserIdAndCarId(userDto.getId(), carId);
        if (ownershipDto.isEmpty() || !ownershipDto.get().isOwner()) { return "redirect:/"; }

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

    @GetMapping("/invite/accept/{carId}")
    public String acceptInviteCoownership(@PathVariable Long carId) {
        Authentication userAuth = SecurityContextHolder.getContext().getAuthentication();
        if (userAuth instanceof AnonymousAuthenticationToken) { return "redirect:/login"; }
        UserDto userDto = userService.getUser(userAuth.getName()).get();

        Optional<CarDto> carDto = carService.findCarById(carId);
        if (carDto.isEmpty()) { return "redirect:/"; }

        Optional<OwnershipDto> invitedOwnershipDto = ownershipService.findFirstByUserIdAndCarId(userDto.getId(), carId);
        if (invitedOwnershipDto.isEmpty() || !invitedOwnershipDto.get().isInvited()) { return "redirect:/"; }

        ownershipService.updateOwnership(invitedOwnershipDto.get().getId(), OwnershipType.COOWNER);
        return "redirect:/car/view/" + carId;
    }

    @GetMapping("/request")
    public String requestCoownership(Model model) {
        Authentication userAuth = SecurityContextHolder.getContext().getAuthentication();
        if (userAuth instanceof AnonymousAuthenticationToken) { return "redirect:/login"; }

        model.addAttribute("requestCoownership", new RequestCoownership());

        return "request";
    }

    @PostMapping("/request")
    public String requestCoownership(@Valid RequestCoownership requestCoownership, BindingResult result, Model model) {
        Authentication userAuth = SecurityContextHolder.getContext().getAuthentication();
        if (userAuth instanceof AnonymousAuthenticationToken) { return "redirect:/login"; }
        UserDto userDto = userService.getUser(userAuth.getName()).get();

        if (result.hasErrors()) {
            return "request";
        }

        Optional<CarDto> carDto = carService.findCarByPlateAndCountryCode(requestCoownership.getPlate(), requestCoownership.getCountryCode());
        if (carDto.isEmpty()) {
            // add car not found error
            return "request";
        }
        if (ownershipService.findFirstByUserIdAndCarId(userDto.getId(), carDto.get().getId()).isPresent()) {
            // add "There already exists a relationship between you and the car" error
            return "request";
            // throw new UniqueException("There already exists a relationship between you and the car");
        }

        ownershipService.saveOwnership(new OwnershipDto(null, userDto, carDto.get(), OwnershipType.REQUESTED));
        return "redirect:/";
    }

    @GetMapping("/request/accept/{carId}/{requestUserUsername}")
    public String acceptRequestCoownership(@PathVariable Long carId, @PathVariable String requestUserUsername) {
        Authentication userAuth = SecurityContextHolder.getContext().getAuthentication();
        if (userAuth instanceof AnonymousAuthenticationToken) { return "redirect:/login"; }
        UserDto userDto = userService.getUser(userAuth.getName()).get();

        Optional<CarDto> carDto = carService.findCarById(carId);
        if (carDto.isEmpty()) { return "redirect:/"; }
        Optional<OwnershipDto> ownershipDto = ownershipService.findFirstByUserIdAndCarId(userDto.getId(), carId);
        if (ownershipDto.isEmpty() || !ownershipDto.get().isOwner()) { return "redirect:/"; }
        Optional<UserDto> requestedUserDto = userService.getUser(requestUserUsername);
        if (requestedUserDto.isEmpty()) { return "redirect:/car/view/" + carId; }

        Optional<OwnershipDto> requestedOwnershipDto = ownershipService.findFirstByUserIdAndCarId(requestedUserDto.get().getId(), carId);
        if (requestedOwnershipDto.isPresent() && requestedOwnershipDto.get().isRequested()) {
            ownershipService.updateOwnership(requestedOwnershipDto.get().getId(), OwnershipType.COOWNER);
        }

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
    public String removeSelfOwnership(@PathVariable Long carId) {
        Authentication userAuth = SecurityContextHolder.getContext().getAuthentication();
        if (userAuth instanceof AnonymousAuthenticationToken) { return "redirect:/login"; }
        return "redirect:" + carId + "/" + userAuth.getName();
    }
}
