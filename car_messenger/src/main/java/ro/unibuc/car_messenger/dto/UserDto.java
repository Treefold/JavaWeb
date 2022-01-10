package ro.unibuc.car_messenger.dto;

import io.swagger.annotations.ApiModelProperty;
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
    @ApiModelProperty(value = "username", required = true, notes = "The username of the User", example = "example@mail.com", position = 1)
    private String username;

    @NotBlank
    @Size(min = 6, max = 20)
    @ApiModelProperty(value = "password", required = true, notes = "The password of the User", example = "Password0.", position = 2)
    private String password;
}
