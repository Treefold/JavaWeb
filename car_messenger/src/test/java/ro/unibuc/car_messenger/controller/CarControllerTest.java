package ro.unibuc.car_messenger.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.ui.Model;
import ro.unibuc.car_messenger.domain.Car;
import ro.unibuc.car_messenger.domain.OwnershipType;
import ro.unibuc.car_messenger.dto.CarDto;
import ro.unibuc.car_messenger.dto.OwnershipDto;
import ro.unibuc.car_messenger.dto.UserDto;
import ro.unibuc.car_messenger.models.CarView;
import ro.unibuc.car_messenger.service.CarService;
import ro.unibuc.car_messenger.service.OwnershipService;
import ro.unibuc.car_messenger.service.UserService;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("h2")
public class CarControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    CarService carService;

    @MockBean
    UserService userService;

    @MockBean
    OwnershipService ownershipService;

    @MockBean
    Model model;

    @Test
//    @WithMockUser
    public void getCarWithoutUser() throws Exception {
        mockMvc.perform(get("/car/view/{carId}", "1"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser
    public void getCarWithID() throws Exception {
        Long carId = 1l;
        UserDto userDto = new UserDto(1l, "test", "test");
        CarDto carDto = new CarDto(carId, "B123ABC", "RO", null);
        OwnershipDto ownershipDto = new OwnershipDto(1l, userDto, carDto, OwnershipType.OWNER);
        CarView carView = new CarView(carDto, Optional.empty());
        carView.addUser(userDto.getId(), ownershipDto.getCategory(), ownershipDto.isOwner());

        when(userService.getUser(anyString())).thenReturn(Optional.of(userDto));
        when(carService.findCarById(carId)).thenReturn(Optional.of(carDto));
        when(ownershipService.findFirstByUserIdAndCarId(carId, userDto.getId())).thenReturn(Optional.of(ownershipDto));
        when(ownershipService.findAllByCarId(carId)).thenReturn(List.of(ownershipDto));
        when(userService.getUser(userDto.getId())).thenReturn(Optional.of(userDto));
        when(userService.getUsers(any())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/car/view/{carId}", "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(view().name("car-view"))
                .andExpect(model().attribute("isAuthorized", ownershipDto.isOwner()))
//                .andExpect(model().attribute("carView", carView))
                .andExpect(model().attribute("ownerUsername", userDto.getUsername()))
                .andExpect(model().attribute("coownersUsernames", Collections.emptyList()))
                .andExpect(model().attribute("pendingInvitations", Collections.emptyList()))
                .andExpect(model().attribute("pendingRequests", Collections.emptyList()));
    }

}
