package com.petsave.petsave.Controller;

import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class OAuth2LoginController {

    @GetMapping("/loginSuccess")
    @ResponseBody
    public String loginSuccess(@AuthenticationPrincipal OAuth2User principal) {
        return "OAuth2 Login Successful! Welcome, " + principal.getAttribute("name");
    }

    @GetMapping("/")
    @ResponseBody
    public String home() {
        return "Welcome to PetSave API!";
    }
}
