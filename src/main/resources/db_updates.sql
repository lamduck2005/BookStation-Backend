-- ✅ THÊM MỚI: Cột hình thức sách
ALTER TABLE book ADD COLUMN book_format VARCHAR(20);

-- ✅ THÊM MỚI: Cập nhật hình thức mặc định cho sách hiện có
UPDATE book SET book_format = 'PAPERBACK' WHERE book_format IS NULL;
