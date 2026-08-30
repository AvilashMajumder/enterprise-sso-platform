package com.enterprise.orgshield.controller;

import com.enterprise.orgshield.dto.ApplicationRequest;
import com.enterprise.orgshield.service.ApplicationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/applications")
public class AdminApplicationController {

    private final ApplicationService applicationService;

    public AdminApplicationController(
            ApplicationService applicationService) {

        this.applicationService = applicationService;
    }

    @GetMapping
    public String applications(Model model) {

        model.addAttribute(
                "applications",
                applicationService.getAllApplications()
        );

        return "applications";
    }

    @GetMapping("/register")
    public String registrationForm(Model model) {

        model.addAttribute(
                "application",
                new ApplicationRequest()
        );

        return "register-application";
    }

    @PostMapping("/register")
    public String registerApplication(
            @ModelAttribute("application")
            ApplicationRequest request) {

        applicationService.registerApplication(request);

        return "redirect:/admin/applications";
    }

    @PostMapping("/{id}/delete")
    public String deleteApplication(
            @PathVariable Long id) {

        applicationService.deleteApplication(id);

        return "redirect:/admin/applications";
    }
}