package ro.unibuc.car_messenger.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class GeneralController {

    @RequestMapping("/home")
    public ModelAndView getHome() {
        ModelAndView model = new ModelAndView("home");
        model.addObject("title", "Home");
        return model;
    }

}
