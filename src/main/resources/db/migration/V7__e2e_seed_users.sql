-- Usuarios y personas para pruebas E2E en la BD de test (mgdb_test).
-- Usa la misma contraseña \"password\" que el usuario admin en V2__seed_data.sql.

-- Personas E2E (company_id = 1 debe existir por V2__seed_data.sql)
INSERT INTO customer (id, first_name, last_name, email, company_id, employee)
VALUES
  (1001, 'Requester E2E', 'User', 'requester_e2e@example.com', 1, FALSE),
  (1002, 'CompanyAdmin E2E', 'User', 'company_admin_e2e@example.com', 1, FALSE),
  (1003, 'Worker E2E', 'User', 'worker_e2e@example.com', 1, TRUE)
ON DUPLICATE KEY UPDATE id = id;

-- Usuarios E2E (perfiles: 2=Requester, 3=Company Admin, 4=Worker; site_id = 1)
INSERT INTO users (id, username, password, customer_id, profile_id, site_id)
VALUES
  (1001, 'requester_e2e', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 1001, 2, 1),
  (1002, 'company_admin_e2e', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 1002, 3, 1),
  (1003, 'worker_e2e', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 1003, 4, 1)
ON DUPLICATE KEY UPDATE id = id;

