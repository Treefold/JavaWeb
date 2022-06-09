package ro.unibuc.car_messenger.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_ACCEPTABLE)
public class UniqueException extends RuntimeException {

    public UniqueException() {}
    public UniqueException(String message) {
        super(message);
    }
    public UniqueException(String message, Throwable throwable) {super(message, throwable);}

}
