package com.fuelflex.platform.assignment.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fuelflex.platform.assignment.entity.EmployeeStationTransfer;

public interface EmployeeStationTransferRepository
        extends JpaRepository<EmployeeStationTransfer, UUID> {
}
