package ro.unibuc.car_messenger.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import ro.unibuc.car_messenger.dto.CarDto;
import ro.unibuc.car_messenger.dto.OwnershipDto;
import ro.unibuc.car_messenger.service.CarService;
import ro.unibuc.car_messenger.service.OwnershipService;
import ro.unibuc.car_messenger.service.UserService;

import java.util.List;

@Controller
public class HomeController {
    @Autowired
    private UserService userService;
    @Autowired
    private CarService carService;
    @Autowired
    private OwnershipService ownershipService;

    @GetMapping({"login","/loginForm"})
    public String loginForm() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/";
        }
        return "login";
    }

    @GetMapping("/login-error")
    public String loginError() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/";
        }
        return "login-error";
    }

    @GetMapping("/access_denied")
    public String accessDenied() { return "access_denied"; }


    @RequestMapping("/")
    public ModelAndView getHome() {
        ModelAndView model = new ModelAndView("home");

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userId = userService.getUser(username).get().getId();

        List<OwnershipDto> ownerships = ownershipService.findAllByUserId(userId);
        List<CarDto> ownedCars = ownerships.stream().filter(o -> o.isOwner())
                .map(ownershipDto -> ownershipDto.getCarDto()).toList();
        List<CarDto> coownedCars = ownerships.stream().filter(o -> o.isCoowner())
                .map(ownershipDto -> ownershipDto.getCarDto()).toList();
        List<CarDto> invitations = ownerships.stream().filter(o -> o.isInvited())
                .map(ownershipDto -> ownershipDto.getCarDto()).toList();
        List<CarDto> requests = ownerships.stream().filter(o -> o.isRequested())
                .map(ownershipDto -> ownershipDto.getCarDto()).toList();

        model.addObject("ownedCars", ownedCars);
        model.addObject("coownedCars", coownedCars);
        model.addObject("invitations", invitations);
        model.addObject("requests", requests);

        return model;
    }

}
