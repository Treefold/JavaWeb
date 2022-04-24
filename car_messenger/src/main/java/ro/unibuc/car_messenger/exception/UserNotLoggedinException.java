package ro.unibuc.car_messenger.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN, reason="Failed to log in, invalid username or password")
public class UserNotLoggedinException extends RuntimeException {

    public UserNotLoggedinException() {}
    public UserNotLoggedinException(String message) {
        super(message);
    }
    public UserNotLoggedinException(String message, Throwable throwable) {super(message, throwable);}

}
