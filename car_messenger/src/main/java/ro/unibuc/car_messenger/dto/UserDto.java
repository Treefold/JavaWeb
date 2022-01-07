package ro.unibuc.car_messenger.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Builder @Data @AllArgsConstructor @NoArgsConstructor
public class UserDto {

    private Long id;

    @NotBlank
    @Size(min = 4, max = 40)
    @Pattern(regexp = "^(.+)@(.+)\\.(.+)$")
    private String username;

    @NotBlank
    @Size(min = 6, max = 20)
    private String password;
}
