package ro.unibuc.car_messenger.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ro.unibuc.car_messenger.domain.EngineType;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Builder @Data @AllArgsConstructor @NoArgsConstructor
public class EngineDto {

    private Long id;

    @NotBlank
    @Size(min = 1, max = 30)
    private String number;

    @NotBlank
    private EngineType type;

}
