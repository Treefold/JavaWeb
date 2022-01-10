package ro.unibuc.car_messenger.dto;

import io.swagger.annotations.ApiModelProperty;
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
    @ApiModelProperty(value = "plate", required = true, notes = "The plate of the Car", example = "B123ABC", position = 1)
    private String plate;

    @NotBlank
    @Size(min = 1, max = 3)
    @ApiModelProperty(value = "countryCode", required = true, notes = "The country code of the Car", example = "RO", position = 2)
    private String countryCode;

}
