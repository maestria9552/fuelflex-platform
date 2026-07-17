package com.fuelflex.platform.role.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fuelflex.platform.role.entity.Role;
import com.fuelflex.platform.role.entity.RoleType;

public interface RoleRepository extends JpaRepository<Role, UUID> {

    Optional<Role> findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCase(String code);

    List<Role> findByTypeAndActiveTrue(RoleType type);

    List<Role> findByOrganizationIdAndActiveTrue(UUID organizationId);
}
