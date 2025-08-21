-- Add quantity column to user_voucher table if not exists
IF NOT EXISTS (
    SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_NAME = 'user_voucher' AND COLUMN_NAME = 'quantity'
)
BEGIN
    ALTER TABLE user_voucher ADD quantity INT NOT NULL DEFAULT 1;
    PRINT 'Added quantity column to user_voucher table';
END
ELSE
BEGIN
    PRINT 'quantity column already exists in user_voucher table';
END

-- Update existing records to have quantity = 1 if NULL
UPDATE user_voucher SET quantity = 1 WHERE quantity IS NULL;
PRINT 'Updated existing user_voucher records with quantity = 1';
