package com.fuelflex.platform.supplier.repository;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
import com.fuelflex.platform.supplier.entity.Supplier;
public interface SupplierRepository extends JpaRepository<Supplier, UUID> { List<Supplier> findByActiveTrueOrderByDisplayNameAsc(); }
