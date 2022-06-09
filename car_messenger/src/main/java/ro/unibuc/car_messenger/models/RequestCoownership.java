package ro.unibuc.car_messenger.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter @Setter @NoArgsConstructor
public class RequestCoownership {

    @NotBlank
    @Size(min = 4, max = 19)
    private String plate;

    @NotBlank
    @Size(min = 1, max = 3)
    private String countryCode;

}
