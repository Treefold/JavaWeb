package ro.unibuc.car_messenger.controller;

import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
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
@Validated
@Api(value = "/user",
        tags = "Users")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private OwnershipService ownershipService;

    @GetMapping()
    @ApiOperation(value = "GetMyUser",
            notes = "Gets the current User details")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "My user details"),
            @ApiResponse(code = 403, message = "Forbidden - UserNotLoggedinException")
    })
    public ResponseEntity<UserView> getMyUser(
            @RequestHeader(value = "login_username", required = false, defaultValue = "") String username,
            @RequestHeader(value = "login_password", required = false, defaultValue = "") String password
    ) {
        UserDto userDto = userService.handleLogin(username, password);
        UserView userView = new UserView(userDto);
        userView.addCars(ownershipService.findAllByUserId(userDto.getId()));
        return ResponseEntity.ok().body(userView);
    }

    @GetMapping("/specific/{userId}")
    @ApiOperation(value = "GetUserById",
            notes = "Gets the specific User details")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "That specific user details"),
            @ApiResponse(code = 404, message = "Not Found")
    })
    public ResponseEntity<UserView> getUserById(
            @RequestHeader(value = "login_username", required = false, defaultValue = "") String username,
            @RequestHeader(value = "login_password", required = false, defaultValue = "") String password,
            @ApiParam(name = "userId", value = "the id of the specified user", required = true)
            @PathVariable Long userId
    ) {
        try {
            userService.handleAdminLogin(username, password);
        }
        catch (Exception e) {
            return ResponseEntity.notFound().build();
        }

        Optional<UserDto> userDto = userService.getUser(userId);
        if (userDto.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        UserView userView = new UserView(userDto.get());
        userView.addCars(ownershipService.findAllByUserId(userId));

        return ResponseEntity.ok().body(userView);
    }

    @GetMapping("/all")
    @ApiOperation(value = "GetAllUsers",
            notes = "Gets all User details")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "All users details"),
            @ApiResponse(code = 404, message = "Not Found")
    })
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
    @ApiOperation(value = "Create an User",
            notes = "Creates a new User based on the information received in the request")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "The user that was just created"),
            @ApiResponse(code = 406, message = "Not Acceptable - InvalidNewUserException")
    })
    public ResponseEntity<UserDto> createUser(
            @ApiParam(name = "user", value = "user details", required = true)
            @RequestBody UserDto userIn
    ) {
        UserDto user = userService.saveUser(userIn);
        userService.addRoleToUser(user.getUsername(), USER);
        return ResponseEntity.created(null).body(user);
    }

    @ApiOperation(value = "Updates the password af an User",
            notes = "Updates the password af an User based on the information received in the request")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "The user that was just updated"),
            @ApiResponse(code = 403, message = "Forbidden - UserNotLoggedinException")
    })
    @PutMapping("/updatePassword")
    public ResponseEntity<Optional<UserDto>> updateUserPassword (
            @RequestHeader(value = "login_username", required = false, defaultValue = "") String username,
            @RequestHeader(value = "login_password", required = false, defaultValue = "") String password,
            @ApiParam(name = "newPassword", value = "the new wanted password", required = true)
            @RequestBody String newPassword
    ) {
        userService.handleLogin(username, password);
        return ResponseEntity.ok().body(userService.updateUser(username, newPassword));
    }

}
