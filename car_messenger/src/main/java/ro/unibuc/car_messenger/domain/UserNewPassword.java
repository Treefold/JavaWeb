package ro.unibuc.car_messenger.domain;

import lombok.Getter;

@Getter
public class UserNewPassword {
    private String username;
    private String password;
    private String newPassword;
}
