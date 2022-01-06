package ro.unibuc.car_messenger.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_ACCEPTABLE, reason="Invalid username or password")
public class InvalidNewUserException extends RuntimeException {
    public InvalidNewUserException() { super(); }
}
