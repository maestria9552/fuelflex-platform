package com.fuelflex.platform.permission.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fuelflex.platform.permission.entity.Permission;

public interface PermissionRepository
        extends JpaRepository<Permission, UUID> {

    Optional<Permission> findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCase(String code);

    List<Permission> findByModuleAndActiveTrue(String module);

    List<Permission> findByActiveTrue();
}
