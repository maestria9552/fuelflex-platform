CREATE TABLE metered_stock_movements (
 id UUID PRIMARY KEY,
 shift_assignment_id UUID NOT NULL REFERENCES pump_shift_assignments(id) ON DELETE RESTRICT,
 station_id UUID NOT NULL REFERENCES stations(id) ON DELETE RESTRICT,
 tank_id UUID NOT NULL REFERENCES tanks(id) ON DELETE RESTRICT,
 product_id UUID NOT NULL REFERENCES products(id) ON DELETE RESTRICT,
 quantity NUMERIC(19,3) NOT NULL,
 movement_type VARCHAR(30) NOT NULL,
 created_by UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
 created_at TIMESTAMPTZ NOT NULL,
 CONSTRAINT uk_metered_movement_shift UNIQUE(shift_assignment_id),
 CONSTRAINT ck_metered_movement_quantity CHECK(quantity>=0),
 CONSTRAINT ck_metered_movement_type CHECK(movement_type='METERED_OUTBOUND')
);
CREATE INDEX idx_metered_movement_tank ON metered_stock_movements(tank_id,created_at);

CREATE TABLE tank_return_source_movements (
 id UUID PRIMARY KEY, tank_return_id UUID NOT NULL REFERENCES tank_returns(id) ON DELETE RESTRICT,
 station_id UUID NOT NULL REFERENCES stations(id) ON DELETE RESTRICT, tank_id UUID NOT NULL REFERENCES tanks(id) ON DELETE RESTRICT,
 product_id UUID NOT NULL REFERENCES products(id) ON DELETE RESTRICT, quantity NUMERIC(19,3) NOT NULL,
 movement_type VARCHAR(40) NOT NULL, created_at TIMESTAMPTZ NOT NULL,
 CONSTRAINT uk_tank_return_source_movement UNIQUE(tank_return_id),
 CONSTRAINT ck_tank_return_source_quantity CHECK(quantity>0),
 CONSTRAINT ck_tank_return_source_type CHECK(movement_type='TANK_RETURN_SOURCE_OUTBOUND')
);
CREATE INDEX idx_tank_return_source_tank ON tank_return_source_movements(tank_id,created_at);

DO $$
BEGIN
 IF EXISTS (
  SELECT 1 FROM pump_shift_assignments shift
  JOIN fuel_meters meter ON meter.id=shift.fuel_meter_id
  LEFT JOIN dispensing_points point ON point.id=meter.dispensing_point_id
  WHERE shift.status='CLOSED' AND COALESCE(point.tank_id,(
   SELECT MIN(candidate.tank_id::text)::uuid FROM dispensing_points candidate
   WHERE candidate.pump_id=meter.pump_id AND candidate.active=true
   HAVING COUNT(DISTINCT candidate.tank_id)=1
  )) IS NULL
 ) THEN RAISE EXCEPTION 'V18 cannot resolve the source tank of every closed shift';
 END IF;
END $$;

INSERT INTO metered_stock_movements(id,shift_assignment_id,station_id,tank_id,product_id,quantity,movement_type,created_by,created_at)
SELECT gen_random_uuid(),shift.id,day.station_id,source.tank_id,tank.product_id,
       ROUND(shift.closing_index-shift.opening_index,3),'METERED_OUTBOUND',shift.closed_by,shift.closed_at
FROM pump_shift_assignments shift
JOIN operational_days day ON day.id=shift.operational_day_id
JOIN fuel_meters meter ON meter.id=shift.fuel_meter_id
LEFT JOIN dispensing_points point ON point.id=meter.dispensing_point_id
CROSS JOIN LATERAL (SELECT COALESCE(point.tank_id,(
 SELECT MIN(candidate.tank_id::text)::uuid FROM dispensing_points candidate
 WHERE candidate.pump_id=meter.pump_id AND candidate.active=true
 HAVING COUNT(DISTINCT candidate.tank_id)=1
)) AS tank_id) source
JOIN tanks tank ON tank.id=source.tank_id
WHERE shift.status='CLOSED';

INSERT INTO tank_return_source_movements(id,tank_return_id,station_id,tank_id,product_id,quantity,movement_type,created_at)
SELECT gen_random_uuid(),returned.id,day.station_id,source.tank_id,tank.product_id,returned.quantity,'TANK_RETURN_SOURCE_OUTBOUND',returned.created_at
FROM tank_returns returned
JOIN pump_shift_assignments shift ON shift.id=returned.shift_assignment_id
JOIN operational_days day ON day.id=shift.operational_day_id
JOIN fuel_meters meter ON meter.id=shift.fuel_meter_id
LEFT JOIN dispensing_points point ON point.id=meter.dispensing_point_id
CROSS JOIN LATERAL (SELECT COALESCE(point.tank_id,(
 SELECT MIN(candidate.tank_id::text)::uuid FROM dispensing_points candidate
 WHERE candidate.pump_id=meter.pump_id AND candidate.active=true
 HAVING COUNT(DISTINCT candidate.tank_id)=1
)) AS tank_id) source
JOIN tanks tank ON tank.id=source.tank_id;
