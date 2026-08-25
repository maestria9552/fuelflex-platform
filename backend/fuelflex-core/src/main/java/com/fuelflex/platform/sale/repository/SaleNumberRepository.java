package com.fuelflex.platform.sale.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import com.fuelflex.platform.sale.entity.FuelSale;

public interface SaleNumberRepository extends Repository<FuelSale, java.util.UUID> {
    @Query(value = "select nextval('fuel_sale_number_seq')", nativeQuery = true)
    long nextValue();
}
