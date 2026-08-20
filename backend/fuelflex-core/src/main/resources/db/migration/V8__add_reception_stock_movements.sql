CREATE TABLE reception_stock_movements (
    id UUID PRIMARY KEY,
    reception_id UUID NOT NULL REFERENCES receptions(id) ON DELETE RESTRICT,
    allocation_id UUID NOT NULL REFERENCES reception_tank_allocations(id) ON DELETE RESTRICT,
    station_id UUID NOT NULL REFERENCES stations(id) ON DELETE RESTRICT,
    tank_id UUID NOT NULL REFERENCES tanks(id) ON DELETE RESTRICT,
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE RESTRICT,
    quantity NUMERIC(19,3) NOT NULL,
    created_by UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_reception_stock_movement_allocation UNIQUE (allocation_id),
    CONSTRAINT ck_reception_stock_movement_quantity CHECK (quantity > 0)
);

CREATE INDEX idx_reception_stock_movement_reception ON reception_stock_movements(reception_id);
CREATE INDEX idx_reception_stock_movement_tank_created ON reception_stock_movements(tank_id, created_at);
