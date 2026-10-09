-- Database Migration for Recommendation System
-- Target DB: fashionecom (MySQL)

CREATE TABLE IF NOT EXISTS `user_behavior_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` INT NULL,
    `session_id` VARCHAR(255) NOT NULL,
    `product_id` INT NULL,
    `event_type` VARCHAR(50) NOT NULL,
    `event_weight` DOUBLE NOT NULL DEFAULT 1.0,
    `search_keyword` VARCHAR(255) NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_ubl_user_created` (`user_id`, `created_at`),
    INDEX `idx_ubl_session_created` (`session_id`, `created_at`),
    INDEX `idx_ubl_product_event_created` (`product_id`, `event_type`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `recommendation_event` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` INT NULL,
    `session_id` VARCHAR(255) NOT NULL,
    `product_id` INT NOT NULL,
    `source` VARCHAR(50) NOT NULL,
    `event_type` VARCHAR(50) NOT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_rec_evt_source_type` (`source`, `event_type`, `created_at`),
    INDEX `idx_rec_evt_product` (`product_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
