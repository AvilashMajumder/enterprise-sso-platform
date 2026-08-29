package com.enterprise.orgshield.service;

import com.enterprise.orgshield.dto.ApplicationRequest;
import com.enterprise.orgshield.entity.Application;
import com.enterprise.orgshield.entity.ApplicationStatus;
import com.enterprise.orgshield.repository.ApplicationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final KeycloakAdminService keycloakAdminService;

    public ApplicationService(
            ApplicationRepository applicationRepository, KeycloakAdminService keycloakAdminService) {

        this.applicationRepository = applicationRepository;
        this.keycloakAdminService = keycloakAdminService;
    }

    public Application registerApplication(
            ApplicationRequest request) {

        if (applicationRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException(
                    "Application name already exists"
            );
        }

        if (applicationRepository.existsByClientId(
                request.getClientId())) {

            throw new IllegalArgumentException(
                    "Client ID already exists"
            );
        }

        Application application = new Application();

        application.setName(request.getName());
        application.setDescription(request.getDescription());
        application.setClientId(request.getClientId());
        application.setRedirectUri(request.getRedirectUri());
        application.setPostLogoutRedirectUri(
                request.getPostLogoutRedirectUri()
        );

        application.setStatus(ApplicationStatus.PENDING);

        Application saved = applicationRepository.save(application);

        try {

            // Create client in Keycloak
            keycloakAdminService.createClient(saved);

            saved.setStatus(ApplicationStatus.ACTIVE);
            applicationRepository.save(saved);

        } catch (Exception e) {

            saved.setStatus(ApplicationStatus.FAILED);
            applicationRepository.save(saved);

            throw e;
        }
        return saved;
    }

    public List<Application> getAllApplications() {

        return applicationRepository.findAll();
    }

    public Application getApplication(Long id) {

        return applicationRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Application not found"
                        )
                );
    }

    public void deleteApplication(Long id) {

        if (!applicationRepository.existsById(id)) {
            throw new IllegalArgumentException(
                    "Application not found"
            );
        }

        applicationRepository.deleteById(id);
    }
}