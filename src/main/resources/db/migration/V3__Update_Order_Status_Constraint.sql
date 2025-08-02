-- Migration script để cập nhật CHECK constraint cho order_status
-- Bổ sung REDELIVERING và RETURNING_TO_WAREHOUSE

-- 1. Drop constraint cũ (nếu có)
IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.CHECK_CONSTRAINTS WHERE CONSTRAINT_NAME = 'CK__order__order_sta__5C8F146D')
BEGIN
    ALTER TABLE [order] DROP CONSTRAINT CK__order__order_sta__5C8F146D
END
GO

-- 2. Thêm constraint mới với đầy đủ trạng thái
ALTER TABLE [order] 
ADD CONSTRAINT CK_order_status 
CHECK (order_status IN (
    'PENDING',
    'CONFIRMED', 
    'SHIPPED',
    'DELIVERED',
    'DELIVERY_FAILED',
    'REDELIVERING',
    'RETURNING_TO_WAREHOUSE',
    'CANCELED',
    'REFUND_REQUESTED',
    'REFUNDING',
    'GOODS_RECEIVED_FROM_CUSTOMER',
    'REFUNDED',
    'GOODS_RETURNED_TO_WAREHOUSE',
    'PARTIALLY_REFUNDED'
))
GO
