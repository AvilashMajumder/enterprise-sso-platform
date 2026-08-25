package com.enterprise.demoapp1.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/profile")
    public String profile(
            @AuthenticationPrincipal OidcUser user,
            Model model) {

        model.addAttribute("name", user.getFullName());
        model.addAttribute("email", user.getEmail());
        model.addAttribute(
                "username",
                user.getPreferredUsername()
        );

        return "profile";
    }
}