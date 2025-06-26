-- Chạy script này trong SQL Server Management Studio hoặc Azure Data Studio
-- để tạo bảng rank thủ công

USE BookStation;

-- Tạo bảng rank
CREATE TABLE rank (
    id INT IDENTITY(1,1) PRIMARY KEY,
    rank_name NVARCHAR(100),
    min_spent DECIMAL(10,2),
    point_multiplier DECIMAL(5,2), 
    created_at BIGINT NOT NULL DEFAULT (DATEDIFF_BIG(MILLISECOND, '1970-01-01 00:00:00', GETDATE())),
    updated_at BIGINT,
    status TINYINT
);

-- Thêm dữ liệu mẫu
INSERT INTO rank (rank_name, min_spent, point_multiplier, created_at, status) VALUES
('Bronze', 0.00, 1.00, DATEDIFF_BIG(MILLISECOND, '1970-01-01 00:00:00', GETDATE()), 1),
('Silver', 1000000.00, 1.25, DATEDIFF_BIG(MILLISECOND, '1970-01-01 00:00:00', GETDATE()), 1),
('Gold', 5000000.00, 1.50, DATEDIFF_BIG(MILLISECOND, '1970-01-01 00:00:00', GETDATE()), 1),


-- Kiểm tra bảng đã tạo thành công
SELECT * FROM rank;
