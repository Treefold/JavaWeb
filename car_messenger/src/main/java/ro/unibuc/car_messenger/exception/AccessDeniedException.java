package ro.unibuc.car_messenger.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN, reason="Not enough privileges")
public class AccessDeniedException extends RuntimeException {

    public AccessDeniedException() {}
    public AccessDeniedException(String message) {
        super(message);
    }
    public AccessDeniedException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
