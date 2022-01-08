package ro.unibuc.car_messenger.models;

import lombok.*;
import ro.unibuc.car_messenger.domain.OwnershipType;
import ro.unibuc.car_messenger.dto.OwnershipDto;
import ro.unibuc.car_messenger.dto.UserDto;

import java.util.ArrayList;
import java.util.List;

@Getter
public class UserView {

    private Long id;
    private String username;
    private String password;

    private List<Long> ownedCarIds;
    private List<Long> coownedCarIds;
    private List<Long> pendingInvitationCarIds;
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

    public void addCar(Long carId, OwnershipType category) {
        switch (category) {
            case OWNER -> this.ownedCarIds.add(carId);
            case COOWNER -> this.coownedCarIds.add(carId);
            case INVITED -> this.pendingInvitationCarIds.add(carId);
            case REQUESTED -> this.pendingRequestCarIds.add(carId);
        }
    }

    public void addCar(List<OwnershipDto> ownershipDto) {
        ownershipDto.forEach(o -> this.addCar(o.getCarDto().getId(), o.getCategory()));
    }
}