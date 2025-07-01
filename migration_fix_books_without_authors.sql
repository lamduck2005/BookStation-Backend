-- MIGRATION SCRIPT: Khắc phục dữ liệu sách không có tác giả
-- Ngày: 01/07/2025
-- Mục đích: Gán tác giả mặc định cho sách chưa có tác giả

-- 1. Tạo tác giả mặc định nếu chưa có
INSERT IGNORE INTO author (author_name, biography, birth_date, created_at, updated_at, created_by, updated_by, status)
VALUES 
('Tác giả chưa xác định', 'Tác giả mặc định cho sách chưa có thông tin tác giả', NULL, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 1, 1, 1);

-- 2. Lấy ID của tác giả mặc định
SET @default_author_id = (SELECT id FROM author WHERE author_name = 'Tác giả chưa xác định' LIMIT 1);

-- 3. Gán tác giả mặc định cho tất cả sách chưa có tác giả
INSERT INTO author_book (author_id, book_id, created_at, updated_at, created_by, updated_by)
SELECT 
    @default_author_id,
    b.id,
    UNIX_TIMESTAMP() * 1000,
    UNIX_TIMESTAMP() * 1000,
    1,
    1
FROM book b
LEFT JOIN author_book ab ON b.id = ab.book_id
WHERE ab.book_id IS NULL;

-- 4. Kiểm tra kết quả
SELECT 
    'BEFORE MIGRATION' as phase,
    COUNT(*) as total_books,
    COUNT(DISTINCT ab.book_id) as books_with_authors,
    COUNT(*) - COUNT(DISTINCT ab.book_id) as books_without_authors
FROM book b
LEFT JOIN author_book ab ON b.id = ab.book_id;

-- 5. Sau migration, tất cả sách phải có tác giả
SELECT 
    'AFTER MIGRATION' as phase,
    COUNT(*) as total_books,
    COUNT(DISTINCT ab.book_id) as books_with_authors,
    COUNT(*) - COUNT(DISTINCT ab.book_id) as books_without_authors
FROM book b
LEFT JOIN author_book ab ON b.id = ab.book_id;

-- 6. Validation: Không được có sách nào không có tác giả
SELECT 
    CASE 
        WHEN COUNT(*) = 0 THEN '✅ MIGRATION SUCCESS: Tất cả sách đều có tác giả'
        ELSE CONCAT('❌ MIGRATION FAILED: Còn ', COUNT(*), ' sách không có tác giả')
    END as migration_status
FROM book b
LEFT JOIN author_book ab ON b.id = ab.book_id
WHERE ab.book_id IS NULL;
