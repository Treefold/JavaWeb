package ro.unibuc.car_messenger.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ro.unibuc.car_messenger.domain.User;
import ro.unibuc.car_messenger.dto.UserDto;
import ro.unibuc.car_messenger.exception.InvalidNewUserException;
import ro.unibuc.car_messenger.mapper.UserMapper;
import ro.unibuc.car_messenger.repo.RoleRepo;
import ro.unibuc.car_messenger.repo.UserRepo;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepo userRepo;
    @Mock
    private UserMapper userMapper;
    @InjectMocks
    private UserService userService;

    @Test
    void saveUser_ExistingUserErr() {
        // arrange
        User existingUser = User.builder().username("test1@mail.com").build();
        UserDto userDtoIn = new UserDto(null, existingUser.getUsername(), "random-pass");
        when(userRepo.findByUsername(userDtoIn.getUsername())).thenReturn(Optional.of(existingUser));

        // act
        RuntimeException exception = assertThrows(InvalidNewUserException.class,
                () -> userService.saveUser(userDtoIn));

        // assert
        assertNull(exception.getMessage());
    }

    @Test
    void saveUser_InvalidUserErr() {
        // arrange
        UserDto userDtoIn = new UserDto(null, "invalid-username", "invalid-password");
        when(userRepo.findByUsername(userDtoIn.getUsername())).thenReturn(Optional.empty());
        when(userMapper.mapToEntity(any())).thenReturn(new User());
        when(userRepo.save(any())).thenThrow(new InvalidNewUserException());

        // act
        RuntimeException exception = assertThrows(InvalidNewUserException.class,
                () -> userService.saveUser(userDtoIn));

        // assert
        assertNull(exception.getMessage());
    }

    @Test
    void saveUser_NoErrors() {
        // arrange
        UserDto userDtoIn = new UserDto(null, "test0@mail.com", "random-password");
        when(userRepo.findByUsername(userDtoIn.getUsername())).thenReturn(Optional.empty());
        when(userMapper.mapToEntity(any())).thenReturn(new User());
        when(userRepo.save(any())).thenReturn(new User());
        when(userMapper.mapToDto(any())).thenReturn(userDtoIn);

        // act
        UserDto newUserDto =  userService.saveUser(userDtoIn);

        // assert
        assertThat(newUserDto).isNotNull();
    }

    @Test
    void updateUser_InexistingUserErr() {
        // arrange
        UserDto userDto = new UserDto(null, "test0@mail.com", "initial-password");
        String newPassword = "new-password";
        when(userRepo.findByUsername(userDto.getUsername())).thenReturn(Optional.empty());

        // act
        Optional<UserDto> updateUser =  userService.updateUser(userDto.getUsername(), newPassword);

        // assert
        assertThat(updateUser).isEmpty();
    }

    @Test
    void updateUser_NoErrors() {
        // arrange
        User initialUser = User.builder().username("test0@mail.com").password("initial-password").build();
        UserDto userDtoOut = UserDto.builder().username(initialUser.getUsername()).password("new-password").build();
        User updatedUser = User.builder().username(initialUser.getUsername()).password(userDtoOut.getPassword()).build();

        when(userRepo.findByUsername(initialUser.getUsername())).thenReturn(Optional.of(initialUser));
        when(userMapper.mapToDto(updatedUser)).thenReturn(userDtoOut);

        // act
        Optional<UserDto> updateUser =  userService.updateUser(initialUser.getUsername(), userDtoOut.getPassword());

        // assert
        assertThat(updateUser).isNotNull();
    }
}