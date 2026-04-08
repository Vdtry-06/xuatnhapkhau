-- Seed du lieu cho nghiep vu:
-- 1. Nhieu dai ly con dat hang online
-- 2. Nhieu supplier cung nhan 1 order
-- 3. Agent chon supplier theo tung dong hang
-- 4. Supplier giao/xuat hang va agent xac nhan DELIVERED

USE agent_db;

INSERT INTO agents (id, name, email, phone, address, tax_code, tier_level, credit_limit, status)
VALUES
  (1, 'Dai ly Ha Noi', 'agent01@example.com', '0901000001', 'Ha Noi', 'AGENT-TAX-001', 'BRONZE', 50000000.00, 'ACTIVE'),
  (2, 'Dai ly Da Nang', 'agent02@example.com', '0901000002', 'Da Nang', 'AGENT-TAX-002', 'SILVER', 80000000.00, 'ACTIVE'),
  (3, 'Dai ly Can Tho', 'agent03@example.com', '0901000003', 'Can Tho', 'AGENT-TAX-003', 'GOLD', 120000000.00, 'ACTIVE'),
  (4, 'Dai ly Hai Phong', 'agent04@example.com', '0901000004', 'Hai Phong', 'AGENT-TAX-004', 'BRONZE', 45000000.00, 'ACTIVE')
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  email = VALUES(email),
  phone = VALUES(phone),
  address = VALUES(address),
  tax_code = VALUES(tax_code),
  tier_level = VALUES(tier_level),
  credit_limit = VALUES(credit_limit),
  status = VALUES(status);

USE supplier_db;

INSERT INTO suppliers (id, name, email, phone, address, tax_code, status)
VALUES
  (1, 'Supplier Sai Gon', 'supplier01@example.com', '0902000001', 'TP HCM', 'SUP-TAX-001', 'ACTIVE'),
  (2, 'Supplier Binh Duong', 'supplier02@example.com', '0902000002', 'Binh Duong', 'SUP-TAX-002', 'ACTIVE'),
  (3, 'Supplier Dong Nai', 'supplier03@example.com', '0902000003', 'Dong Nai', 'SUP-TAX-003', 'ACTIVE'),
  (4, 'Supplier Bac Ninh', 'supplier04@example.com', '0902000004', 'Bac Ninh', 'SUP-TAX-004', 'ACTIVE')
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  email = VALUES(email),
  phone = VALUES(phone),
  address = VALUES(address),
  tax_code = VALUES(tax_code),
  status = VALUES(status);

INSERT INTO supplier_inventory (
  id, supplier_id, supplier_name, product_id, product_name,
  available_quantity, production_capacity, lead_time_days, supply_price, can_fulfill_extra, note
)
VALUES
  (1, 1, 'Supplier Sai Gon', 1, 'May dong goi mini', 5, 20, 7, 14500000.00, 1, 'Gia tot, giao nhanh khu vuc Nam'),
  (2, 2, 'Supplier Binh Duong', 1, 'May dong goi mini', 3, 25, 10, 14300000.00, 1, 'Can dat coc 30%'),
  (3, 3, 'Supplier Dong Nai', 1, 'May dong goi mini', 1, 40, 14, 14100000.00, 1, 'Gia thap, lead time dai hon'),
  (4, 1, 'Supplier Sai Gon', 2, 'Linh kien co khi', 30, 100, 5, 3100000.00, 1, 'Co san nhieu linh kien'),
  (5, 2, 'Supplier Binh Duong', 2, 'Linh kien co khi', 20, 120, 6, 3050000.00, 1, 'Nhan don so luong lon'),
  (6, 4, 'Supplier Bac Ninh', 2, 'Linh kien co khi', 15, 130, 8, 3000000.00, 1, 'Phu hop don vung Bac'),
  (7, 3, 'Supplier Dong Nai', 3, 'Bao bi cong nghiep', 100, 500, 3, 1200000.00, 1, 'Bao bi co san'),
  (8, 4, 'Supplier Bac Ninh', 3, 'Bao bi cong nghiep', 80, 600, 4, 1180000.00, 1, 'Gia canh tranh'),
  (9, 1, 'Supplier Sai Gon', 4, 'Cuon mang PE', 150, 800, 2, 540000.00, 1, 'Giao ngay trong 24h'),
  (10, 2, 'Supplier Binh Duong', 4, 'Cuon mang PE', 120, 900, 2, 530000.00, 1, 'Gia uu dai theo so luong')
ON DUPLICATE KEY UPDATE
  supplier_name = VALUES(supplier_name),
  product_name = VALUES(product_name),
  available_quantity = VALUES(available_quantity),
  production_capacity = VALUES(production_capacity),
  lead_time_days = VALUES(lead_time_days),
  supply_price = VALUES(supply_price),
  can_fulfill_extra = VALUES(can_fulfill_extra),
  note = VALUES(note);

USE product_db;

INSERT INTO categories (id, name, description)
VALUES
  (1, 'Hang dat truoc', 'Danh muc cho nghiep vu dat hang online cua dai ly con')
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  description = VALUES(description);

INSERT INTO products (id, sku, name, description, import_price, export_price, stock_quantity, supplier_id, supplier_name, category_id)
VALUES
  (1, 'PRE-001', 'May dong goi mini', 'San pham dat truoc, cho supplier nhan va bao kha nang cung ung', 12000000.00, 15000000.00, 20, NULL, NULL, 1),
  (2, 'PRE-002', 'Linh kien co khi', 'Co the duoc nhieu supplier cung nhan tren cung order', 2500000.00, 3200000.00, 50, NULL, NULL, 1),
  (3, 'PRE-003', 'Bao bi cong nghiep', 'Dat online, supplier xac nhan so luong san co va san xuat them', 900000.00, 1250000.00, 120, NULL, NULL, 1),
  (4, 'PRE-004', 'Cuon mang PE', 'San pham tieu hao thuong xuyen cua dai ly con', 420000.00, 560000.00, 200, NULL, NULL, 1)
ON DUPLICATE KEY UPDATE
  sku = VALUES(sku),
  name = VALUES(name),
  description = VALUES(description),
  import_price = VALUES(import_price),
  export_price = VALUES(export_price),
  stock_quantity = VALUES(stock_quantity),
  supplier_id = VALUES(supplier_id),
  supplier_name = VALUES(supplier_name),
  category_id = VALUES(category_id);

USE auth_db;

INSERT INTO app_users (id, username, password, role, agent_id, supplier_id, enabled)
VALUES
  (2, 'agent01', '$2a$10$DBwCs1WVUCZ5gQQJtK8qvuVzFu0sRJb1HuR4nxgrs3uoJ7uw26nde', 'AGENT', 1, NULL, 1),
  (3, 'agent02', '$2a$10$DBwCs1WVUCZ5gQQJtK8qvuVzFu0sRJb1HuR4nxgrs3uoJ7uw26nde', 'AGENT', 2, NULL, 1),
  (4, 'agent03', '$2a$10$DBwCs1WVUCZ5gQQJtK8qvuVzFu0sRJb1HuR4nxgrs3uoJ7uw26nde', 'AGENT', 3, NULL, 1),
  (5, 'agent04', '$2a$10$DBwCs1WVUCZ5gQQJtK8qvuVzFu0sRJb1HuR4nxgrs3uoJ7uw26nde', 'AGENT', 4, NULL, 1),
  (6, 'supplier01', '$2a$10$cyRFZJgQ84c55E6y4JcXfuqYFCZR24zSzS4sfY5ZinLNU8dLY4hdS', 'SUPPLIER', NULL, 1, 1),
  (7, 'supplier02', '$2a$10$cyRFZJgQ84c55E6y4JcXfuqYFCZR24zSzS4sfY5ZinLNU8dLY4hdS', 'SUPPLIER', NULL, 2, 1),
  (8, 'supplier03', '$2a$10$cyRFZJgQ84c55E6y4JcXfuqYFCZR24zSzS4sfY5ZinLNU8dLY4hdS', 'SUPPLIER', NULL, 3, 1),
  (9, 'supplier04', '$2a$10$cyRFZJgQ84c55E6y4JcXfuqYFCZR24zSzS4sfY5ZinLNU8dLY4hdS', 'SUPPLIER', NULL, 4, 1)
ON DUPLICATE KEY UPDATE
  username = VALUES(username),
  password = VALUES(password),
  role = VALUES(role),
  agent_id = VALUES(agent_id),
  supplier_id = VALUES(supplier_id),
  enabled = VALUES(enabled);

-- Tai khoan mac dinh:
-- admin / admin123 (duoc seed boi auth-service)
-- agent01..agent04 / agent123
-- supplier01..supplier04 / supplier123
