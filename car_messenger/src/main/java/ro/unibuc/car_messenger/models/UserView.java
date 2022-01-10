package ro.unibuc.car_messenger.models;

import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import ro.unibuc.car_messenger.domain.OwnershipType;
import ro.unibuc.car_messenger.dto.OwnershipDto;
import ro.unibuc.car_messenger.dto.UserDto;

import java.util.ArrayList;
import java.util.List;

@Getter
public class UserView {

    private Long id;
    @ApiModelProperty(value = "username", required = true, notes = "The username of the User", example = "example@mail.com", position = 1)
    private String username;
    @ApiModelProperty(value = "password", required = true, notes = "The password of the User", example = "Password0.", position = 2)
    private String password;

    @ApiModelProperty(value = "ownedCarIds", notes = "The owned cars ids of the User", position = 3)
    private List<Long> ownedCarIds;
    @ApiModelProperty(value = "coownedCarIds", notes = "The coowned cars ids of the User", position = 4)
    private List<Long> coownedCarIds;
    @ApiModelProperty(value = "pendingInvitationCarIds", notes = "The invitations received to the cars (by ids) of the User", position = 5)
    private List<Long> pendingInvitationCarIds;
    @ApiModelProperty(value = "pendingRequestCarIds", notes = "The requests send to the cars (by ids) of the User", position = 6)
    private List<Long> pendingRequestCarIds;

    public UserView(UserDto userDto) {
        this.id = userDto.getId();
        this.username = userDto.getUsername();
        this.password = userDto.getPassword();
        this.ownedCarIds = new ArrayList<>();
        this.coownedCarIds = new ArrayList<>();
        this.pendingInvitationCarIds = new ArrayList<>();
        this.pendingRequestCarIds = new ArrayList<>();
    }

    public UserView addCar(Long carId, OwnershipType category) {
        switch (category) {
            case OWNER -> this.ownedCarIds.add(carId);
            case COOWNER -> this.coownedCarIds.add(carId);
            case INVITED -> this.pendingInvitationCarIds.add(carId);
            case REQUESTED -> this.pendingRequestCarIds.add(carId);
        }
        return this;
    }

    public UserView addCars(List<OwnershipDto> ownershipDto) {
        ownershipDto.forEach(o -> this.addCar(o.getCarDto().getId(), o.getCategory()));
        return this;
    }
}