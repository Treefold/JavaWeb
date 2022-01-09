package ro.unibuc.car_messenger.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ro.unibuc.car_messenger.domain.OwnershipType;

import static ro.unibuc.car_messenger.domain.OwnershipType.*;

@Builder @Data @AllArgsConstructor @NoArgsConstructor
public class OwnershipDto {

    private Long id;
    private UserDto userDto;
    private CarDto carDto;
    private OwnershipType category;

    public boolean isOwner     () { return this.category == OWNER; }
    public boolean isCoowner   () { return this.category == COOWNER; }
    public boolean isInvited   () { return this.category == INVITED; }
    public boolean isRequested () { return this.category == REQUESTED; }
    public boolean isAtLeastCoowner () {return this.isOwner() || this.isCoowner();}
}
