package ro.unibuc.car_messenger.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Builder @Data @AllArgsConstructor @NoArgsConstructor
public class CarDto {

    private Long id;

    @NotBlank
    @Size(min = 4, max = 19)
    private String plate;

    @NotBlank
    @Size(min = 1, max = 3)
    private String countryCode;

}
