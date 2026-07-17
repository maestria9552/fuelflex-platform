package com.fuelflex.platform.organization.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fuelflex.platform.organization.entity.Organization;
import com.fuelflex.platform.organization.entity.OrganizationStatus;

public interface OrganizationRepository
        extends JpaRepository<Organization, UUID> {

    Optional<Organization> findByCodeIgnoreCase(String code);

    Optional<Organization> findByEmailIgnoreCase(String email);

    Optional<Organization> findByRegistrationNumberIgnoreCase(
            String registrationNumber
    );

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByRegistrationNumberIgnoreCase(
            String registrationNumber
    );

    List<Organization> findByStatusAndActiveTrue(
            OrganizationStatus status
    );

    List<Organization> findByActiveTrue();
}
