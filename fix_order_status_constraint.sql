-- SCRIPT SỬA NHANH CHO DATABASE BOOKSTATION
-- Chạy script này để sửa CHECK constraint

USE BookStation;
GO

-- 1. Kiểm tra constraint hiện tại
SELECT CONSTRAINT_NAME, CHECK_CLAUSE 
FROM INFORMATION_SCHEMA.CHECK_CONSTRAINTS 
WHERE TABLE_NAME = 'order';
GO

-- 2. Drop constraint cũ
DECLARE @ConstraintName NVARCHAR(200)
SELECT @ConstraintName = CONSTRAINT_NAME 
FROM INFORMATION_SCHEMA.CHECK_CONSTRAINTS 
WHERE TABLE_NAME = 'order' AND COLUMN_NAME = 'order_status'

IF @ConstraintName IS NOT NULL
BEGIN
    DECLARE @SQL NVARCHAR(MAX) = 'ALTER TABLE [order] DROP CONSTRAINT ' + @ConstraintName
    EXEC sp_executesql @SQL
    PRINT 'Đã xóa constraint: ' + @ConstraintName
END
GO

-- 3. Tạo constraint mới với đầy đủ trạng thái
ALTER TABLE [order] 
ADD CONSTRAINT CK_order_status_new
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
));
GO

PRINT 'Đã tạo constraint mới thành công!';

-- 4. Kiểm tra lại
SELECT CONSTRAINT_NAME, CHECK_CLAUSE 
FROM INFORMATION_SCHEMA.CHECK_CONSTRAINTS 
WHERE TABLE_NAME = 'order';
GO
