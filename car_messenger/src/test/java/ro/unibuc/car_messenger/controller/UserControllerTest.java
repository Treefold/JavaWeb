package ro.unibuc.car_messenger.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ro.unibuc.car_messenger.dto.UserDto;
import ro.unibuc.car_messenger.exception.InvalidNewUserException;
import ro.unibuc.car_messenger.exception.UserNotLoggedinException;
import ro.unibuc.car_messenger.service.CarService;
import ro.unibuc.car_messenger.service.OwnershipService;
import ro.unibuc.car_messenger.service.UserService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
class UserControllerTest {

    @MockBean
    private UserService userService;
    @MockBean
    private OwnershipService ownershipService;
    @MockBean
    private CarService carService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createUser_ExistingUserErr() throws Exception {
        //Arrange
        UserDto userDtoIn = UserDto.builder().username("test0@mail.com").password("password").build();
        when(userService.saveUser(any())).thenThrow(new InvalidNewUserException());

        //Act
        mockMvc.perform(post("/user/create")
                        .content(objectMapper.writeValueAsString(userDtoIn))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotAcceptable());

        //Assert
        verify(userService, times(1)).saveUser(any());
        verify(userService, never()).addRoleToUser(any(), any());
    }

    @Test
    void createUser_NoError() throws Exception {
        //Arrange
        UserDto userDtoIn = UserDto.builder().username("test0@mail.com").password("password").build();
        when(userService.saveUser(any())).thenReturn(userDtoIn);

        //Act
        mockMvc.perform(post("/user/create")
                        .content(objectMapper.writeValueAsString(userDtoIn))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        //Assert
        verify(userService, times(1)).saveUser(any());
        verify(userService, times(1)).addRoleToUser(any(), any());
    }

    @Test
    void updateUserPassword_UserNotFoundErr() throws Exception {
        //Arrange
        String newPassword = "new-password";
        when(userService.handleLogin(any(), any())).thenThrow(new UserNotLoggedinException());

        //Act
        mockMvc.perform(put("/user/updatePassword")
                        .content(newPassword)
                        .contentType(MediaType.TEXT_PLAIN))
                .andExpect(status().isForbidden());

        //Assert
        verify(userService, times(1)).handleLogin(any(), any());
        verify(userService, never()).updateUser(any(), any());
    }

    @Test
    void updateUserPassword_NoError() throws Exception {
        //Arrange
        String newPassword = "new-password";

        //Act
        mockMvc.perform(put("/user/updatePassword")
                        .content(newPassword)
                        .contentType(MediaType.TEXT_PLAIN))
                .andExpect(status().isOk());

        //Assert
        verify(userService, times(1)).handleLogin(any(), any());
        verify(userService, times(1)).updateUser(any(), any());
    }

}