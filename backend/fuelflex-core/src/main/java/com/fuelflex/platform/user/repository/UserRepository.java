package com.fuelflex.platform.user.repository;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.fuelflex.platform.role.entity.RoleType;
import com.fuelflex.platform.user.entity.User;
import jakarta.persistence.LockModeType;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmailIgnoreCase(String email);

    Optional<User> findByPhoneNumber(String phoneNumber);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    boolean existsByPhoneNumberAndIdNot(String phoneNumber, UUID id);

    boolean existsByOperationalCode(String operationalCode);

    Optional<User> findByIdAndOrganizationId(UUID id, UUID organizationId);

    @Query("select distinct user from User user join user.roles role where user.organization.id = :organizationId and user.enabled = true and role.active = true and upper(role.code) = upper(:roleCode)")
    java.util.List<User> findEnabledByOrganizationIdAndRoleCode(@Param("organizationId") UUID organizationId, @Param("roleCode") String roleCode);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select user from User user where user.id = :id and user.organization.id = :organizationId")
    Optional<User> lockByIdAndOrganizationId(
            @Param("id") UUID id, @Param("organizationId") UUID organizationId);


    @Query("""
            select distinct user
              from User user
              join user.roles role
             where user.organization.id = :organizationId
               and role.active = true
               and role.code in :visibleRoleCodes
               and role.code = coalesce(:roleCode, role.code)
               and user.enabled = coalesce(:enabled, user.enabled)
               and not exists (
                    select scopedUser.id
                      from User scopedUser
                      join scopedUser.roles scopedRole
                     where scopedUser = user
                       and scopedRole.type = :excludedRoleType
               )
               and (
                    coalesce(:search, '') = ''
                    or lower(user.firstName) like lower(concat('%', :search, '%'))
                    or lower(user.lastName) like lower(concat('%', :search, '%'))
                    or lower(user.email) like lower(concat('%', :search, '%'))
                    or lower(user.phoneNumber) like lower(concat('%', :search, '%'))
                    or lower(concat(user.firstName, ' ', user.lastName))
                       like lower(concat('%', :search, '%'))
               )
            """)
    Page<User> findEmployees(
            @Param("organizationId") UUID organizationId,
            @Param("visibleRoleCodes") Collection<String> visibleRoleCodes,
            @Param("search") String search,
            @Param("roleCode") String roleCode,
            @Param("enabled") Boolean enabled,
            @Param("excludedRoleType") RoleType excludedRoleType,
            Pageable pageable
    );
}
