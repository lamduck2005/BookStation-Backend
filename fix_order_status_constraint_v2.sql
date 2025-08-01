-- SCRIPT SỬA NHANH CHO DATABASE BOOKSTATION (SQL Server)
-- Chạy script này để sửa CHECK constraint

USE BookStation;
GO

-- 1. Kiểm tra constraint hiện tại
SELECT cc.name AS ConstraintName, cc.definition AS CheckClause
FROM sys.check_constraints cc
INNER JOIN sys.tables t ON cc.parent_object_id = t.object_id
WHERE t.name = 'order';
GO

-- 2. Drop constraint cũ nếu tồn tại
IF EXISTS (SELECT * FROM sys.check_constraints cc 
           INNER JOIN sys.tables t ON cc.parent_object_id = t.object_id 
           WHERE t.name = 'order' AND cc.name LIKE '%order_status%')
BEGIN
    DECLARE @ConstraintName NVARCHAR(200)
    SELECT @ConstraintName = cc.name 
    FROM sys.check_constraints cc
    INNER JOIN sys.tables t ON cc.parent_object_id = t.object_id
    WHERE t.name = 'order' AND cc.name LIKE '%order_status%'
    
    DECLARE @SQL NVARCHAR(MAX) = 'ALTER TABLE [order] DROP CONSTRAINT ' + QUOTENAME(@ConstraintName)
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
SELECT cc.name AS ConstraintName, cc.definition AS CheckClause
FROM sys.check_constraints cc
INNER JOIN sys.tables t ON cc.parent_object_id = t.object_id
WHERE t.name = 'order';
GO
