package ro.unibuc.car_messenger.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.unibuc.car_messenger.domain.Role;
import ro.unibuc.car_messenger.domain.RoleType;
import ro.unibuc.car_messenger.domain.User;
import ro.unibuc.car_messenger.dto.UserDto;
import ro.unibuc.car_messenger.exception.AccessDeniedException;
import ro.unibuc.car_messenger.exception.InvalidNewUserException;
import ro.unibuc.car_messenger.exception.UserNotLoggedinException;
import ro.unibuc.car_messenger.mapper.UserMapper;
import ro.unibuc.car_messenger.repo.RoleRepo;
import ro.unibuc.car_messenger.repo.UserRepo;

import javax.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service @Transactional @Slf4j
public class UserService {
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private RoleRepo roleRepo;
    @Autowired
    private UserMapper userMapper;

    public UserDto saveUser(UserDto userDto) {
        if (userRepo.findByUsername(userDto.getUsername()).isPresent()) {
            throw new InvalidNewUserException(); // the user already exists
        }
        UserDto newUserDto;

        try {
            User userDraft = userMapper.mapToEntity(userDto);
            userDraft.setRoles(new ArrayList<>());
            newUserDto = userMapper.mapToDto(userRepo.save(userDraft));
            log.info("Saving new user {{}} to the database", userDto.getUsername());
        } catch (ConstraintViolationException e) {
            throw new InvalidNewUserException(); // email or password validation error
        }
        return newUserDto;
    }

    public Optional<UserDto> updateUser(String username, String password) {
        Optional<User> user = userRepo.findByUsername(username);
        if (user.isEmpty()) { return Optional.empty(); }
        user.get().setPassword(password);
        log.info("Updating user with username{{}} in the database", username);
        return Optional.of(userMapper.mapToDto(user.get()));
    }

    public Role saveRole(Role role) {
        log.info("Saving new role with name{{}} to the database", role.getName());
        return roleRepo.save(role);
    }

    public void addRoleToUser(String username, RoleType roleType) {
        Optional<User> user = userRepo.findByUsername(username);
        if (user.isEmpty()) { return; }
        Role role = roleRepo.findByName(roleType);
        log.info("Add role {{}} to username {{}}", roleType, username);
        user.get().getRoles().add(role);
    }

    public Optional<UserDto> getUser(Long id) {
        log.info("Fetching user id{{}}", id);
        Optional<User> user = userRepo.findById(id);
        return user.map(u -> userMapper.mapToDto(u));
    }

    public List<UserDto> getUsers (List<Long> ids) {
        log.info("Fetching users ids{{}}", ids);
        List<User> users = userRepo.findAllById(ids);
        return users.stream().map(u -> userMapper.mapToDto(u)).toList();
    }

    public Optional<UserDto> getUser(String username) {
        log.info("Fetching user name{{}}", username);
        Optional<User> user = userRepo.findByUsername(username);
        return user.map(u -> userMapper.mapToDto(u));
    }

    public List<UserDto> getUsers() {
        log.info("Fetching all users");
        return userRepo.findAll().stream().map(u -> userMapper.mapToDto(u)).collect(Collectors.toList());
    }

    public Optional<UserDto> login(String username, String password) {
        boolean loginSuccess;
        Optional<User> user = userRepo.findByUsername(username);
        if (user.isEmpty()) { loginSuccess = false; }
        else { loginSuccess = user.get().testPassword(password); }
        log.info("{} login for username{{}}", loginSuccess ? "Successful" : "Failed", username);
        return loginSuccess ? Optional.of(userMapper.mapToDto(user.get())) : Optional.empty();
    }

    public UserDto handleLogin (String username, String password) {
        if ("".equals(username) || "".equals(password)) { throw new UserNotLoggedinException(); }
        Optional<UserDto> userDto = this.login(username, password);
        if (userDto.isEmpty()) { throw new UserNotLoggedinException(); }
        return userDto.get();
    }

    public UserDto handleAdminLogin (String username, String password) {
        UserDto userDto = this.handleLogin (username, password);
        userRepo.findByUsername(userDto.getUsername()).ifPresent(
                (u) -> { if (!u.isAdmin()) { throw new AccessDeniedException(); } }
        );
        return userDto;
    }
}
