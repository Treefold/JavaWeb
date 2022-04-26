package ro.unibuc.car_messenger.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Getter @Setter @NoArgsConstructor
public class InviteCoowner {

    @Size(min = 4, max = 40)
    @Pattern(regexp = "^(.+)@(.+)\\.(.+)$")
    private String username;

}
