-- Tạo tất cả databases
CREATE DATABASE IF NOT EXISTS auth_db    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS agent_db   CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS product_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS supplier_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS order_db   CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS payment_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Cấp toàn quyền cho user xnk trên tất cả databases
GRANT ALL PRIVILEGES ON auth_db.*    TO 'xnk'@'%';
GRANT ALL PRIVILEGES ON agent_db.*   TO 'xnk'@'%';
GRANT ALL PRIVILEGES ON product_db.* TO 'xnk'@'%';
GRANT ALL PRIVILEGES ON supplier_db.* TO 'xnk'@'%';
GRANT ALL PRIVILEGES ON order_db.*   TO 'xnk'@'%';
GRANT ALL PRIVILEGES ON payment_db.* TO 'xnk'@'%';

FLUSH PRIVILEGES;
