package org.example.backendcrcoach.web.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class RootController {

    private static final String REDIRECT_URL = "https://ricitos2001.github.io/Backend-CRCoach/";

    @GetMapping("/")
    public String rootRedirect() {
        return "redirect:" + REDIRECT_URL;
    }

    @RequestMapping("/error")
    public String errorRedirect() {
        return "redirect:" + REDIRECT_URL;
    }
}

