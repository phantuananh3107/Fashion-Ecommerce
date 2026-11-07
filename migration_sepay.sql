-- ============================================================
-- Migration SePay QR Payment — fashion-ecommerce
-- Idempotent for MySQL 5.7 / 8.0 (uses information_schema + PREPARE/EXECUTE)
-- ------------------------------------------------------------
-- NOTE: Không dùng syntax IF NOT EXISTS của MariaDB.
-- ------------------------------------------------------------

DELIMITER $$

-- 1️⃣ Add column `payment_code` (unique string) if it does not exist
CREATE PROCEDURE add_payment_code_if_missing()
BEGIN
    DECLARE cnt INT DEFAULT 0;
    SELECT COUNT(*) INTO cnt
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME   = 'Order'
      AND COLUMN_NAME  = 'payment_code';
    IF cnt = 0 THEN
        SET @sql = CONCAT('ALTER TABLE `Order` ADD COLUMN `payment_code` VARCHAR(32) NULL UNIQUE COMMENT ''',
                          'Mã đơn dùng trong nội dung chuyển khoản (DH + orderId)',
                          '''');
        PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
    END IF;
END$$

CALL add_payment_code_if_missing();
DROP PROCEDURE IF EXISTS add_payment_code_if_missing;$$

-- 2️⃣ Add column `paid_at` if missing
CREATE PROCEDURE add_paid_at_if_missing()
BEGIN
    DECLARE cnt INT DEFAULT 0;
    SELECT COUNT(*) INTO cnt
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME   = 'Order'
      AND COLUMN_NAME  = 'paid_at';
    IF cnt = 0 THEN
        SET @sql = 'ALTER TABLE `Order` ADD COLUMN `paid_at` DATETIME NULL COMMENT ''Thời điểm thanh toán thành công''';
        PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
    END IF;
END$$

CALL add_paid_at_if_missing();
DROP PROCEDURE IF EXISTS add_paid_at_if_missing;$$

-- 3️⃣ Add column `expires_at` if missing
CREATE PROCEDURE add_expires_at_if_missing()
BEGIN
    DECLARE cnt INT DEFAULT 0;
    SELECT COUNT(*) INTO cnt
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME   = 'Order'
      AND COLUMN_NAME  = 'expires_at';
    IF cnt = 0 THEN
        SET @sql = 'ALTER TABLE `Order` ADD COLUMN `expires_at` DATETIME NULL COMMENT ''Thời điểm hết hạn QR code''';
        PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
    END IF;
END$$

CALL add_expires_at_if_missing();
DROP PROCEDURE IF EXISTS add_expires_at_if_missing;$$

-- 4️⃣ Create table `payment_transaction` (idempotent, CREATE TABLE IF NOT EXISTS is supported)
CREATE TABLE IF NOT EXISTS `payment_transaction` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `sepay_transaction_id` VARCHAR(64) NOT NULL COMMENT 'ID giao dịch từ SePay (trường id trong webhook)',
    `order_id` INT NOT NULL,
    `amount` DECIMAL(15,2) NOT NULL,
    `content` VARCHAR(500) NULL,
    `raw_payload` TEXT NULL COMMENT 'Toàn bộ JSON body từ SePay',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uq_sepay_tx` (`sepay_transaction_id`),
    KEY `idx_pt_order` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Bảng ghi nhận giao dịch SePay — chống trùng lặp';$$

-- 5️⃣ Index for fast scan of pending SePay orders (placed at end of file)
CREATE PROCEDURE add_idx_order_sepay_pending()
BEGIN
    DECLARE idx_cnt INT DEFAULT 0;
    SELECT COUNT(*) INTO idx_cnt
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME   = 'Order'
      AND INDEX_NAME   = 'idx_order_sepay_pending';
    IF idx_cnt = 0 THEN
        SET @sql = 'CREATE INDEX `idx_order_sepay_pending` ON `Order` (payment_method, payment_status, expires_at)';
        PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
    END IF;
END$$

CALL add_idx_order_sepay_pending();
DROP PROCEDURE IF EXISTS add_idx_order_sepay_pending;$$

DELIMITER ;
