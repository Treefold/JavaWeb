package ro.unibuc.car_messenger.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ro.unibuc.car_messenger.dto.UserDto;
import ro.unibuc.car_messenger.models.UserView;
import ro.unibuc.car_messenger.service.OwnershipService;
import ro.unibuc.car_messenger.service.UserService;

import java.util.List;
import java.util.Optional;

import static ro.unibuc.car_messenger.domain.RoleType.USER;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Validated
public class UserController {
    private final UserService userService;
    private final OwnershipService ownershipService;

    @GetMapping()
    public ResponseEntity<UserView> getMyUser(
            @RequestHeader(value = "login_username", required = false, defaultValue = "") String username,
            @RequestHeader(value = "login_password", required = false, defaultValue = "") String password
    ) {
        UserDto userDto = userService.handleLogin(username, password);
        UserView userView = new UserView(userDto);
        userView.addCar(ownershipService.findAllByUserId(userDto.getId()));
        return ResponseEntity.ok().body(userView);
    }

    @GetMapping("/specific/{id}")
    public ResponseEntity<UserView> getUserById(
            @RequestHeader(value = "login_username", required = false, defaultValue = "") String username,
            @RequestHeader(value = "login_password", required = false, defaultValue = "") String password,
            @PathVariable Long id
    ) {
        try {
            userService.handleAdminLogin(username, password);
        }
        catch (Exception e) {
            return ResponseEntity.notFound().build();
        }

        Optional<UserDto> userDto = userService.getUser(id);
        if (userDto.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        UserView userView = new UserView(userDto.get());
        userView.addCar(ownershipService.findAllByUserId(id));

        return ResponseEntity.ok().body(userView);
    }

    @GetMapping("/all")
    public ResponseEntity<List<UserDto>> getAllUsers(
            @RequestHeader(value = "login_username", required = false, defaultValue = "") String username,
            @RequestHeader(value = "login_password", required = false, defaultValue = "") String password
    ) {
        try {
            userService.handleAdminLogin(username, password);
        }
        catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().body(userService.getUsers());
    }

    @PostMapping("/create")
    public ResponseEntity<UserDto> saveUser(@RequestBody UserDto userIn) {
        UserDto user = userService.saveUser(userIn);
        userService.addRoleToUser(user.getUsername(), USER);
        return ResponseEntity.created(null).body(user);
    }

    @PutMapping("/updatePassword")
    public ResponseEntity<Optional<UserDto>> updateUserPassword (
            @RequestHeader(value = "login_username", required = false, defaultValue = "") String username,
            @RequestHeader(value = "login_password", required = false, defaultValue = "") String password,
            @RequestBody String newPassword
    ) {
        userService.handleLogin(username, password);
        return ResponseEntity.ok().body(userService.updateUser(username, newPassword));
    }

}
