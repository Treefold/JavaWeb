package ro.unibuc.car_messenger.models;

import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import ro.unibuc.car_messenger.domain.Engine;
import ro.unibuc.car_messenger.domain.EngineType;
import ro.unibuc.car_messenger.domain.OwnershipType;
import ro.unibuc.car_messenger.dto.CarDto;
import ro.unibuc.car_messenger.dto.OwnershipDto;

import java.util.ArrayList;
import java.util.List;

@Getter
public class CarView {

    private Long id;
    @ApiModelProperty(value = "plate", required = true, notes = "The plate of the Car", example = "B123ABC", position = 1)
    private String plate;
    @ApiModelProperty(value = "countryCode", required = true, notes = "The country code of the Car", example = "RO", position = 2)
    private String countryCode;
    private String engineNumber = null;
    private EngineType engineType = null;

    @ApiModelProperty(value = "ownerUserId", required = true, notes = "The owner Id of the Car", position = 3)
    private Long ownerUserId;
    @ApiModelProperty(value = "coownerUserIds", notes = "The coowners (by id) of the Car", position = 4)
    private List<Long> coownerUserIds;
    @ApiModelProperty(value = "pendingInvitationUserIds", notes = "The invited users (by id) to the Car", position = 5)
    private List<Long> pendingInvitationUserIds;
    @ApiModelProperty(value = "pendingRequestUserIds", notes = "The requested users (by id) of the Car", position = 6)
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

    public CarView(CarDto carDto, Engine engine) {
        this(carDto);
        if (engine != null) {
            this.engineNumber = engine.getNumber();
            this.engineType = engine.getType();
        }
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
