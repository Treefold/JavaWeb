package ro.unibuc.car_messenger.models;

import lombok.*;
import ro.unibuc.car_messenger.domain.OwnershipType;
import ro.unibuc.car_messenger.dto.CarDto;
import ro.unibuc.car_messenger.dto.OwnershipDto;

import java.util.ArrayList;
import java.util.List;

@Getter
public class CarView {

    private Long id;
    private String plate;
    private String countryCode;

    private Long ownerUserId;
    private List<Long> coownerUserIds;
    private List<Long> pendingInvitationUserIds;
    private List<Long> pendingRequestUserIds;



    public CarView(CarDto carDto) {
        this.id = carDto.getId();
        this.plate = carDto.getPlate();
        this.countryCode = carDto.getCountryCode();
        this.ownerUserId = 0L;
        this.coownerUserIds = new ArrayList<>();
        this.pendingInvitationUserIds = new ArrayList<>();
        this.pendingRequestUserIds = new ArrayList<>();
    }

    public CarView addUser(Long userId, OwnershipType category, boolean isOwner) {
        switch (category) {
            case OWNER -> this.ownerUserId = userId;
            case COOWNER -> this.coownerUserIds.add(userId);
            case INVITED -> { if(isOwner){ this.pendingInvitationUserIds.add(userId); } }
            case REQUESTED -> { if(isOwner){ this.pendingRequestUserIds.add(userId); } }
        }
        return this;
    }

    public CarView addUsers(List<OwnershipDto> ownershipDto, boolean isOwner) {
        ownershipDto.forEach(o -> this.addUser(o.getUserDto().getId(), o.getCategory(), isOwner));
        return this;
    }

}
