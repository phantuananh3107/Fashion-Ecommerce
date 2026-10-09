-- Demo Seeder SQL Script for User Behavior Log and Recommendation Events
-- Target DB: fashionecom

-- Clear previous demo data if any (idempotent)
DELETE FROM user_behavior_log WHERE session_id LIKE 'demo_session_%' OR user_id BETWEEN 900 AND 920;
DELETE FROM recommendation_event WHERE session_id LIKE 'demo_session_%' OR user_id BETWEEN 900 AND 920;

-- Insert behavior log data for 20 mock users/sessions with distinct category interests
-- Group 1 (Users 901-905): Category 1 (Áo / Tops)
INSERT INTO user_behavior_log (user_id, session_id, product_id, event_type, event_weight, search_keyword, created_at)
SELECT 901, 'demo_session_901', p.id, 'VIEW', 1.0, NULL, NOW() - INTERVAL FLOOR(RAND()*10) DAY
FROM Product p WHERE p.category_id = 1 AND p.isDeleted = FALSE LIMIT 3;

INSERT INTO user_behavior_log (user_id, session_id, product_id, event_type, event_weight, search_keyword, created_at)
SELECT 901, 'demo_session_901', p.id, 'ADD_TO_CART', 3.0, NULL, NOW() - INTERVAL 2 DAY
FROM Product p WHERE p.category_id = 1 AND p.isDeleted = FALSE LIMIT 2;

INSERT INTO user_behavior_log (user_id, session_id, product_id, event_type, event_weight, search_keyword, created_at)
SELECT 902, 'demo_session_902', p.id, 'VIEW', 1.0, NULL, NOW() - INTERVAL 5 DAY
FROM Product p WHERE p.category_id = 1 AND p.isDeleted = FALSE LIMIT 4;

-- Group 2 (Users 906-910): Category 2 (Quần / Bottoms)
INSERT INTO user_behavior_log (user_id, session_id, product_id, event_type, event_weight, search_keyword, created_at)
SELECT 906, 'demo_session_906', p.id, 'VIEW', 1.0, NULL, NOW() - INTERVAL FLOOR(RAND()*8) DAY
FROM Product p WHERE p.category_id = 2 AND p.isDeleted = FALSE LIMIT 3;

INSERT INTO user_behavior_log (user_id, session_id, product_id, event_type, event_weight, search_keyword, created_at)
SELECT 906, 'demo_session_906', p.id, 'PURCHASE', 5.0, NULL, NOW() - INTERVAL 1 DAY
FROM Product p WHERE p.category_id = 2 AND p.isDeleted = FALSE LIMIT 1;

INSERT INTO user_behavior_log (user_id, session_id, product_id, event_type, event_weight, search_keyword, created_at)
SELECT 907, 'demo_session_907', p.id, 'ADD_TO_CART', 3.0, NULL, NOW() - INTERVAL 3 DAY
FROM Product p WHERE p.category_id = 2 AND p.isDeleted = FALSE LIMIT 3;

-- Group 3 (Users 911-915): Category 3 (Đầm / Váy)
INSERT INTO user_behavior_log (user_id, session_id, product_id, event_type, event_weight, search_keyword, created_at)
SELECT 911, 'demo_session_911', p.id, 'VIEW', 1.0, NULL, NOW() - INTERVAL FLOOR(RAND()*12) DAY
FROM Product p WHERE p.category_id = 3 AND p.isDeleted = FALSE LIMIT 4;

INSERT INTO user_behavior_log (user_id, session_id, product_id, event_type, event_weight, search_keyword, created_at)
SELECT 912, 'demo_session_912', p.id, 'SEARCH', 1.0, 'váy xòe', NOW() - INTERVAL 4 DAY
FROM Product p WHERE p.category_id = 3 AND p.isDeleted = FALSE LIMIT 2;

-- Group 4 (Users 916-920): General / High Activity
INSERT INTO user_behavior_log (user_id, session_id, product_id, event_type, event_weight, search_keyword, created_at)
SELECT 916, 'demo_session_916', p.id, 'VIEW', 1.0, NULL, NOW() - INTERVAL FLOOR(RAND()*6) DAY
FROM Product p WHERE p.isDeleted = FALSE ORDER BY RAND() LIMIT 5;

-- Insert recommendation CTR tracking events for admin dashboard demo
INSERT INTO recommendation_event (user_id, session_id, product_id, source, event_type, created_at)
SELECT 901, 'demo_session_901', p.id, 'HOME_PERSONAL', 'IMPRESSION', NOW() - INTERVAL 2 DAY
FROM Product p WHERE p.isDeleted = FALSE LIMIT 5;

INSERT INTO recommendation_event (user_id, session_id, product_id, source, event_type, created_at)
SELECT 901, 'demo_session_901', p.id, 'HOME_PERSONAL', 'CLICK', NOW() - INTERVAL 2 DAY
FROM Product p WHERE p.isDeleted = FALSE LIMIT 2;

INSERT INTO recommendation_event (user_id, session_id, product_id, source, event_type, created_at)
SELECT 906, 'demo_session_906', p.id, 'DETAIL_ALSO_BOUGHT', 'IMPRESSION', NOW() - INTERVAL 1 DAY
FROM Product p WHERE p.isDeleted = FALSE LIMIT 4;

INSERT INTO recommendation_event (user_id, session_id, product_id, source, event_type, created_at)
SELECT 906, 'demo_session_906', p.id, 'DETAIL_ALSO_BOUGHT', 'CLICK', NOW() - INTERVAL 1 DAY
FROM Product p WHERE p.isDeleted = FALSE LIMIT 1;
