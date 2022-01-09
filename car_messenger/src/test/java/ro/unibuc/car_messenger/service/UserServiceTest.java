package ro.unibuc.car_messenger.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ro.unibuc.car_messenger.domain.Role;
import ro.unibuc.car_messenger.domain.RoleType;
import ro.unibuc.car_messenger.domain.User;
import ro.unibuc.car_messenger.dto.UserDto;
import ro.unibuc.car_messenger.exception.InvalidNewUserException;
import ro.unibuc.car_messenger.mapper.UserMapper;
import ro.unibuc.car_messenger.repo.RoleRepo;
import ro.unibuc.car_messenger.repo.UserRepo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepo userRepo;
    @Mock
    private RoleRepo roleRepo;
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
        verify(userRepo, times(1)).findByUsername(any());
        verify(userMapper, never()).mapToEntity(any());
        verify(userRepo, never()).save(any());
        verify(userMapper, never()).mapToDto(any());
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
        verify(userRepo, times(1)).findByUsername(any());
        verify(userMapper, times(1)).mapToEntity(any());
        verify(userRepo, times(1)).save(any());
        verify(userMapper, never()).mapToDto(any());
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
        verify(userRepo, times(1)).findByUsername(any());
        verify(userMapper, times(1)).mapToEntity(any());
        verify(userRepo, times(1)).save(any());
        verify(userMapper, times(1)).mapToDto(any());
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
        verify(userRepo, times(1)).findByUsername(any());
        verify(userMapper, never()).mapToDto(any());
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
        verify(userRepo, times(1)).findByUsername(any());
        verify(userMapper, times(1)).mapToDto(any());
    }

    @Test
    void saveRole_NoErrors() {
        // arrange
        Role roleIn = new Role(null, RoleType.ADMIN);
        when(roleRepo.save(roleIn)).thenReturn(new Role(1L, RoleType.ADMIN));

        // act
        Role roleReturn =  userService.saveRole(roleIn);

        // assert
        assertThat(roleReturn).isNotNull();
        verify(roleRepo, times(1)).save(any());
    }

    @Test
    void addRoleToUser_UserNotFoundErr() {
        // arrange
        Role roleIn = new Role(null, RoleType.ADMIN);
        User user = User.builder().username("test0@mail.com").build();
        when(userRepo.findByUsername(user.getUsername())).thenReturn(Optional.empty());

        // act
        userService.addRoleToUser(user.getUsername(), roleIn.getName());

        // assert
         verify(userRepo, times(1)).findByUsername(any());
        verify(roleRepo, never()).findByName(any());
    }

    @Test
    void addRoleToUser_NoError() {
        // arrange
        Role roleIn = new Role(null, RoleType.ADMIN);
        User user = User.builder().username("test0@mail.com").roles(new ArrayList<>()).build();
        when(userRepo.findByUsername(user.getUsername())).thenReturn(Optional.of(user));

        // act
        userService.addRoleToUser(user.getUsername(), roleIn.getName());

        // assert
        verify(userRepo, times(1)).findByUsername(any());
        verify(roleRepo, times(1)).findByName(any());
    }

    @Test
    void getUserById_UserNotFoundErr() {
        // arrange
        User user = User.builder().id(1L).build();
        when(userRepo.findById(user.getId())).thenReturn(Optional.empty());

        // act
        Optional<UserDto> userDtoOut = userService.getUser(user.getId());

        // assert
        verify(userRepo, times(1)).findById(any());
        verify(userMapper, never()).mapToDto(any());
        assertThat(userDtoOut).isEmpty();
    }

    @Test
    void getUserById_NoError() {
        // arrange
        User user = User.builder().id(1L).build();
        when(userRepo.findById(user.getId())).thenReturn(Optional.of(user));
        when(userMapper.mapToDto(any())).thenReturn(new UserDto());

        // act
        Optional<UserDto> userDtoOut = userService.getUser(user.getId());

        // assert
        verify(userRepo, times(1)).findById(any());
        verify(userMapper, times(1)).mapToDto(any());
        assertThat(userDtoOut).isPresent();
    }

    @Test
    void getUserByUsername_UserNotFoundErr() {
        // arrange
        User user = User.builder().username("test0@mail.com").build();
        when(userRepo.findByUsername(user.getUsername())).thenReturn(Optional.empty());

        // act
        Optional<UserDto> userDtoOut = userService.getUser(user.getUsername());

        // assert
        verify(userRepo, times(1)).findByUsername(any());
        verify(userMapper, never()).mapToDto(any());
        assertThat(userDtoOut).isEmpty();
    }

    @Test
    void getUserByUsername_NoError() {
        // arrange
        User user = User.builder().username("test0@mail.com").build();
        when(userRepo.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(userMapper.mapToDto(any())).thenReturn(new UserDto());

        // act
        Optional<UserDto> userDtoOut = userService.getUser(user.getUsername());

        // assert
        verify(userRepo, times(1)).findByUsername(any());
        verify(userMapper, times(1)).mapToDto(any());
        assertThat(userDtoOut).isPresent();
    }

    @Test
    void getUsers_NoUsers() {
        // arrange
        List<User> users = new ArrayList<>();
        when(userRepo.findAll()).thenReturn(users);
//        when(userMapper.mapToDto(any())).thenReturn(new UserDto());

        // act
        List<UserDto> userDtosOut = userService.getUsers();

        // assert
        verify(userRepo, times(1)).findAll();
        verify(userMapper, never()).mapToDto(any());
        assertThat(userDtosOut).isEmpty();
    }

    @Test
    void getUsers_SomeUsers() {
        // arrange
        int nUsers = 10;
        List<User> users = new ArrayList<>();
        for (var i = 0; i < nUsers; ++i) { users.add(new User()); }

        when(userRepo.findAll()).thenReturn(users);
        when(userMapper.mapToDto(any())).thenReturn(new UserDto());

        // act
        List<UserDto> userDtosOut = userService.getUsers();

        // assert
        verify(userRepo, times(1)).findAll();
        verify(userMapper, times(nUsers)).mapToDto(any());
        assertEquals(nUsers, userDtosOut.size());
    }

}