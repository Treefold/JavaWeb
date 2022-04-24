package ro.unibuc.car_messenger.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class HomeController {

    @GetMapping("/loginForm")
    public String loginForm() { return "login"; }

    @GetMapping("/login-error")
    public String loginError() { return "login-error"; }

    @GetMapping("/access_denied")
    public String accessDenied() { return "access_denied"; }

    @RequestMapping({"", "/", "/index","/home"})
    public ModelAndView getHome() {
        ModelAndView model = new ModelAndView("home");
        model.addObject("title", "Home");
        return model;
    }

}
