package ro.unibuc.car_messenger.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.unibuc.car_messenger.domain.Role;
import ro.unibuc.car_messenger.domain.RoleType;
import ro.unibuc.car_messenger.domain.User;
import ro.unibuc.car_messenger.exception.AccessDeniedException;
import ro.unibuc.car_messenger.exception.InvalidNewUserException;
import ro.unibuc.car_messenger.exception.UserNotLoggedinException;
import ro.unibuc.car_messenger.repo.RoleRepo;
import ro.unibuc.car_messenger.repo.UserRepo;

import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.Optional;

@Service @RequiredArgsConstructor @Transactional @Slf4j
public class UserService {
    private final UserRepo userRepo;
    private final RoleRepo roleRepo;

    public User saveUser(User user) {
        if (userRepo.findByUsername(user.getUsername()).isPresent()) {
            throw new InvalidNewUserException(); // the user already exists
        }
        User newUser;
        try {
            newUser = userRepo.save(user);
            log.info("Saving new user {} to the database", user.getUsername());
        } catch (ConstraintViolationException e) {
            throw new InvalidNewUserException(); // email or password validation error
        }
        return newUser;
    }

    public Optional<User> updateUser(String username, String password) {
        log.info("Updating user {} in the database", username);
        Optional<User> user = userRepo.findByUsername(username);
        user.ifPresent(u -> u.setPassword(password));
        return user;
    }

    public Role saveRole(Role role) {
        log.info("Saving new role {} to the database", role.getName());
        return roleRepo.save(role);
    }

    public void addRoleToUser(String username, RoleType roleType) {
        log.info("Add role {} to user {}", roleType, username);
        Optional<User> user = userRepo.findByUsername(username);
        Role role = roleRepo.findByName(roleType);
        user.ifPresent(u -> u.getRoles().add(role));
    }

    public Optional<User> getUser(String username) {
        log.info("Fetching user {}", username);
        return userRepo.findByUsername(username);
    }

    public List<User> getUsers() {
        log.info("Fetching all users");
        return userRepo.findAll();
    }

    public Optional<User> login(String username, String password) {
        boolean loginSuccess;
        Optional<User> user = userRepo.findByUsername(username);
        if (user.isEmpty()) { loginSuccess = false; }
        else { loginSuccess = user.get().testPassword(password); }
        log.info("{} login for {}", loginSuccess ? "Successful" : "Failed", username);
        return loginSuccess ? user : Optional.empty();
    }

    public User handleLogin (String username, String password) {
        if (username.equals("") || password.equals("")) { throw new UserNotLoggedinException(); }
        Optional<User> user = this.login(username, password);
        if (user.isEmpty()) { throw new UserNotLoggedinException(); }
        return user.get();
    }

    public User handleAdminLogin (String username, String password) {
        User user = this.handleLogin (username, password);
        if (!user.isAdmin()) { throw new AccessDeniedException(); }
        return user;
    }
}
