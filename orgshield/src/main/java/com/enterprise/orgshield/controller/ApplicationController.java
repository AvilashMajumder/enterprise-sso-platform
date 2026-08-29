package com.enterprise.orgshield.controller;

import com.enterprise.orgshield.dto.ApplicationRequest;
import com.enterprise.orgshield.entity.Application;
import com.enterprise.orgshield.service.ApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(
            ApplicationService applicationService) {

        this.applicationService = applicationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Application registerApplication(
            @Valid @RequestBody ApplicationRequest request) {

        return applicationService.registerApplication(request);
    }

    @GetMapping
    public List<Application> getApplications() {

        return applicationService.getAllApplications();
    }

    @GetMapping("/{id}")
    public Application getApplication(
            @PathVariable Long id) {

        return applicationService.getApplication(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteApplication(
            @PathVariable Long id) {

        applicationService.deleteApplication(id);
    }
}