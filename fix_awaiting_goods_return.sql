-- SCRIPT SỬA DATABASE CHO TRẠNG THÁI MỚI: AWAITING_GOODS_RETURN
-- Chạy script này để thêm trạng thái mới

USE BookStation;
GO

-- 1. Xóa constraint cũ
IF EXISTS (SELECT * FROM sys.check_constraints cc 
           INNER JOIN sys.tables t ON cc.parent_object_id = t.object_id 
           WHERE t.name = 'order')
BEGIN
    DECLARE @ConstraintName NVARCHAR(200)
    
    -- Xóa tất cả constraints liên quan đến order_status
    DECLARE constraint_cursor CURSOR FOR
    SELECT cc.name 
    FROM sys.check_constraints cc
    INNER JOIN sys.tables t ON cc.parent_object_id = t.object_id
    WHERE t.name = 'order' AND cc.name LIKE '%order_status%'
    
    OPEN constraint_cursor
    FETCH NEXT FROM constraint_cursor INTO @ConstraintName
    
    WHILE @@FETCH_STATUS = 0
    BEGIN
        DECLARE @SQL NVARCHAR(MAX) = 'ALTER TABLE [order] DROP CONSTRAINT ' + QUOTENAME(@ConstraintName)
        EXEC sp_executesql @SQL
        PRINT 'Đã xóa constraint: ' + @ConstraintName
        
        FETCH NEXT FROM constraint_cursor INTO @ConstraintName
    END
    
    CLOSE constraint_cursor
    DEALLOCATE constraint_cursor
END
GO

-- 2. Tạo constraint mới với AWAITING_GOODS_RETURN
ALTER TABLE [order]
ADD CONSTRAINT CK_order_status_final
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
    'AWAITING_GOODS_RETURN',
    'GOODS_RECEIVED_FROM_CUSTOMER',
    'GOODS_RETURNED_TO_WAREHOUSE',
    'REFUNDING',
    'REFUNDED',
    'PARTIALLY_REFUNDED'
));
GO

PRINT 'Đã cập nhật constraint với trạng thái AWAITING_GOODS_RETURN thành công!';

-- 3. Kiểm tra lại
SELECT cc.name AS ConstraintName, cc.definition AS CheckClause
FROM sys.check_constraints cc
INNER JOIN sys.tables t ON cc.parent_object_id = t.object_id
WHERE t.name = 'order';
GO
