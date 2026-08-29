package com.enterprise.orgshield.repository;

import com.enterprise.orgshield.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApplicationRepository
        extends JpaRepository<Application, Long> {

    Optional<Application> findByClientId(String clientId);

    boolean existsByName(String name);

    boolean existsByClientId(String clientId);
}