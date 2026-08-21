ALTER TABLE purchase_order_history
DROP CONSTRAINT ck_purchase_order_history_action;

ALTER TABLE purchase_order_history
ADD CONSTRAINT ck_purchase_order_history_action
CHECK (action IN (
    'ORDER_CREATED',
    'ORDER_UPDATED',
    'ORDER_SUBMITTED',
    'SUPERVISOR_APPROVED',
    'SUPERVISOR_REJECTED',
    'SUPPLIER_APPROVED',
    'SUPPLIER_REJECTED',
    'PARTIALLY_RECEIVED',
    'RECEIVED'
));
