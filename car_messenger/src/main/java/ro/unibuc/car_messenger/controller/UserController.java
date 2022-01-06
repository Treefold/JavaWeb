package ro.unibuc.car_messenger.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ro.unibuc.car_messenger.domain.User;
import ro.unibuc.car_messenger.domain.UserNewPassword;
import ro.unibuc.car_messenger.exception.AccessDeniedException;
import ro.unibuc.car_messenger.exception.UserNotLoggedinException;
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

    @GetMapping()
    public ResponseEntity<List<User>> getUsers(
            @RequestHeader(value = "login_username", required = false, defaultValue = "") String username,
            @RequestHeader(value = "login_password", required = false, defaultValue = "") String password
    ) {
        userService.handleAdminLogin(username, password);
        return ResponseEntity.ok().body(userService.getUsers());
    }

    @PostMapping("/create")
    public ResponseEntity<User> saveUser(@RequestBody User userIn) {
        User user = userService.saveUser(userIn);
        userService.addRoleToUser(user.getUsername(), USER);
        return ResponseEntity.created(null).body(user);
    }

    @PutMapping("/updatePassword")
    public ResponseEntity<Optional<User>> updateUserPassword (
            @RequestHeader(value = "login_username", required = false, defaultValue = "") String username,
            @RequestHeader(value = "login_password", required = false, defaultValue = "") String password,
            @RequestBody String newPassword
    ) {
        userService.handleLogin(username, password);
        return ResponseEntity.ok().body(userService.updateUser(username, newPassword));
    }

}
