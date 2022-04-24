package ro.unibuc.car_messenger.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import ro.unibuc.car_messenger.exception.*;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(AccessDeniedException.class)
    public ModelAndView handlerAccessDeniedException(Exception exception){
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("access_denied");
        modelAndView.getModel().put("exception",exception);
        return modelAndView;
    }

    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    @ExceptionHandler(InvalidNewUserException.class)
    public ModelAndView handlerInvalidNewUserException(Exception exception){
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("login-error");
        modelAndView.getModel().put("exception",exception);
        return modelAndView;
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ModelAndView handlerNotFoundException(Exception exception){
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("notfound");
        modelAndView.getModel().put("exception",exception);
        return modelAndView;
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(UniqueException.class)
    public ModelAndView handlerUserNotLoggedinException(Exception exception){
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("login");
        modelAndView.getModel().put("exception",exception);
        return modelAndView;
    }
}
