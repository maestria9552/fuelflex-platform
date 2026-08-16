package com.fuelflex.platform.purchaseorder.repository;
import org.springframework.jdbc.core.JdbcTemplate; import org.springframework.stereotype.Repository; import lombok.RequiredArgsConstructor;
@Repository @RequiredArgsConstructor
public class PurchaseOrderNumberRepository {
    private final JdbcTemplate jdbc;
    public long nextValue(){ return jdbc.queryForObject("select nextval('purchase_order_number_seq')", Long.class); }
}
