-- Tạo bảng rank nếu chưa tồn tại
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='rank' AND xtype='U')
BEGIN
    CREATE TABLE rank (
        id INT IDENTITY(1,1) PRIMARY KEY,
        rank_name NVARCHAR(100),
        min_spent DECIMAL(10,2),
        point_multiplier DECIMAL(5,2),
        created_at BIGINT NOT NULL DEFAULT (DATEDIFF_BIG(MILLISECOND, '1970-01-01 00:00:00', GETDATE())),
        updated_at BIGINT,
        status TINYINT
    );
END;

-- Thêm dữ liệu mẫu nếu bảng trống
IF (SELECT COUNT(*) FROM rank) = 0
BEGIN
    INSERT INTO rank (rank_name, min_spent, point_multiplier, created_at, status) VALUES
    ('Bronze', 0.00, 1.00, DATEDIFF_BIG(MILLISECOND, '1970-01-01 00:00:00', GETDATE()), 1),
    ('Silver', 1000000.00, 1.25, DATEDIFF_BIG(MILLISECOND, '1970-01-01 00:00:00', GETDATE()), 1),
    ('Gold', 5000000.00, 1.50, DATEDIFF_BIG(MILLISECOND, '1970-01-01 00:00:00', GETDATE()), 1),
    ('Platinum', 10000000.00, 2.00, DATEDIFF_BIG(MILLISECOND, '1970-01-01 00:00:00', GETDATE()), 1);
END;
