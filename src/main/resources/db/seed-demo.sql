-- Datos demo para explorar dashboards (prefijo demo_) en perfil test.
-- No afecta JUnit (reqtest_) ni E2E (e2e_). Los tests solo limpian e2e_ y reqtest_.
-- Incluye múltiples empresas y sitios para la tabla "Por empresa y sitio".

-- Empresas y sitios demo (IDs 2001+)
INSERT INTO company (id, name, description, company_type) VALUES
(2001, 'demo_Empresa Norte', 'Demo servicios norte', 'COMPANY'),
(2002, 'demo_Empresa Sur', 'Demo servicios sur', 'COMPANY'),
(2003, 'demo_Empresa Centro', 'Demo servicios centro', 'COMPANY')
ON DUPLICATE KEY UPDATE id = id;

INSERT INTO site (id, name, description, company_id) VALUES
(2001, 'demo_Sede Central Norte', 'Oficina principal', 2001),
(2002, 'demo_Sucursal Norte A', 'Almacén zona norte', 2001),
(2003, 'demo_Oficina Sur', 'Sede sur', 2002),
(2004, 'demo_Planta Sur', 'Planta industrial', 2002),
(2005, 'demo_Despacho Centro', 'Oficinas centro', 2003)
ON DUPLICATE KEY UPDATE id = id;

-- Usuarios demo (company 1 existente + company 2001 para diversidad)
INSERT INTO customer (id, first_name, last_name, email, company_id, employee) VALUES
(2001, 'demo_Demo', 'Requester', 'demo_requester@example.com', 1, FALSE),
(2002, 'demo_Demo', 'Worker1', 'demo_worker1@example.com', 1, TRUE),
(2003, 'demo_Demo', 'Worker2', 'demo_worker2@example.com', 1, TRUE),
(2004, 'demo_Req', 'Norte', 'demo_req_norte@example.com', 2001, FALSE)
ON DUPLICATE KEY UPDATE id = id;

INSERT INTO users (id, username, password, customer_id, profile_id, site_id) VALUES
(2001, 'demo_requester', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 2001, 2, 1),
(2002, 'demo_worker1', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 2002, 4, 1),
(2003, 'demo_worker2', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 2003, 4, 1),
(2004, 'demo_req_norte', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 2004, 2, 2001)
ON DUPLICATE KEY UPDATE id = id;

-- Requests demo: distribuir entre site 1 (company 1), sites 2001-2005 (companies 2001-2003)
-- Estados variados para gráficos pie y tabla por empresa/sitio
INSERT INTO request (description, location, site_id, user_id, service_category_id, service_sub_category_id, request_status, create_date, priority) VALUES
('demo_Pendiente 1', 'Oficina A', 1, 2001, 1, 1, 'PENDING_APPROVAL', NOW() - INTERVAL 2 DAY, 'L'),
('demo_Pendiente 2', 'Oficina B', 1, 2001, 1, 2, 'PENDING_APPROVAL', NOW() - INTERVAL 1 DAY, 'M'),
('demo_Creada', 'Planta 1', 1, 2001, 2, 3, 'CREATED', NOW() - INTERVAL 3 DAY, 'H'),
('demo_Asignada 1', 'Planta 2', 1, 2001, 1, 1, 'ASSIGNED', NOW() - INTERVAL 4 DAY, 'M'),
('demo_En tránsito', 'Recepción', 1, 2001, 2, 3, 'IN_TRANSIT', NOW() - INTERVAL 6 DAY, 'H'),
('demo_Completada', 'Sala reuniones', 1, 2001, 1, 1, 'DONE', NOW() - INTERVAL 7 DAY, 'M'),
('demo_Rechazada', 'Exterior', 1, 2001, 1, 2, 'REJECTED', NOW() - INTERVAL 8 DAY, 'L'),
('demo_Valorada 1', 'Zona común', 1, 2001, 2, 3, 'RATED', NOW() - INTERVAL 10 DAY, 'H'),
('demo_Valorada 2', 'Despacho 1', 1, 2001, 1, 1, 'RATED', NOW() - INTERVAL 12 DAY, 'M'),
('demo_Valorada 3', 'Cocina', 1, 2001, 2, 3, 'RATED', NOW() - INTERVAL 15 DAY, 'L'),
('demo_Mes pasado', 'Pasillo', 1, 2001, 1, 1, 'CREATED', NOW() - INTERVAL 35 DAY, 'L'),
('demo_Norte pendiente', 'Sede Norte', 2001, 2004, 1, 1, 'PENDING_APPROVAL', NOW() - INTERVAL 1 DAY, 'M'),
('demo_Norte creada', 'Sede Norte', 2001, 2004, 2, 3, 'CREATED', NOW() - INTERVAL 3 DAY, 'H'),
('demo_Norte asignada', 'Sede Norte', 2001, 2004, 1, 1, 'ASSIGNED', NOW() - INTERVAL 4 DAY, 'L'),
('demo_Norte valorada', 'Sede Norte', 2001, 2004, 1, 2, 'RATED', NOW() - INTERVAL 5 DAY, 'M'),
('demo_Sucursal A pend', 'Sucursal A', 2002, 2004, 1, 2, 'PENDING_APPROVAL', NOW() - INTERVAL 2 DAY, 'H'),
('demo_Sucursal A creada', 'Sucursal A', 2002, 2004, 2, 3, 'CREATED', NOW() - INTERVAL 6 DAY, 'L'),
('demo_Sucursal A valorada', 'Sucursal A', 2002, 2004, 1, 1, 'RATED', NOW() - INTERVAL 8 DAY, 'M'),
('demo_Sur oficina creada', 'Oficina Sur', 2003, 2001, 1, 1, 'CREATED', NOW() - INTERVAL 4 DAY, 'M'),
('demo_Sur oficina valorada', 'Oficina Sur', 2003, 2001, 2, 3, 'RATED', NOW() - INTERVAL 7 DAY, 'H'),
('demo_Sur planta transit', 'Planta Sur', 2004, 2001, 1, 2, 'IN_TRANSIT', NOW() - INTERVAL 2 DAY, 'L'),
('demo_Sur planta done', 'Planta Sur', 2004, 2001, 1, 1, 'DONE', NOW() - INTERVAL 5 DAY, 'M'),
('demo_Centro despacho', 'Despacho Centro', 2005, 2001, 2, 3, 'RATED', NOW() - INTERVAL 3 DAY, 'H'),
('demo_Centro despacho 2', 'Despacho Centro', 2005, 2001, 1, 1, 'ASSIGNED', NOW() - INTERVAL 9 DAY, 'L');

-- Asignaciones para ASSIGNED, IN_TRANSIT, DONE, RATED
INSERT INTO request_assignment (request_id, user_id)
SELECT r.id, 2002 FROM request r WHERE r.description = 'demo_Asignada 1' LIMIT 1;
INSERT INTO request_assignment (request_id, user_id)
SELECT r.id, 2002 FROM request r WHERE r.description = 'demo_En tránsito' LIMIT 1;
INSERT INTO request_assignment (request_id, user_id)
SELECT r.id, 2002 FROM request r WHERE r.description = 'demo_Completada' LIMIT 1;
INSERT INTO request_assignment (request_id, user_id)
SELECT r.id, 2002 FROM request r WHERE r.description = 'demo_Valorada 1' LIMIT 1;
INSERT INTO request_assignment (request_id, user_id)
SELECT r.id, 2002 FROM request r WHERE r.description = 'demo_Valorada 2' LIMIT 1;
INSERT INTO request_assignment (request_id, user_id)
SELECT r.id, 2003 FROM request r WHERE r.description = 'demo_Valorada 3' LIMIT 1;
INSERT INTO request_assignment (request_id, user_id)
SELECT r.id, 2002 FROM request r WHERE r.description = 'demo_Norte asignada' LIMIT 1;
INSERT INTO request_assignment (request_id, user_id)
SELECT r.id, 2002 FROM request r WHERE r.description = 'demo_Norte valorada' LIMIT 1;
INSERT INTO request_assignment (request_id, user_id)
SELECT r.id, 2002 FROM request r WHERE r.description = 'demo_Sucursal A valorada' LIMIT 1;
INSERT INTO request_assignment (request_id, user_id)
SELECT r.id, 2002 FROM request r WHERE r.description = 'demo_Sur oficina valorada' LIMIT 1;
INSERT INTO request_assignment (request_id, user_id)
SELECT r.id, 2002 FROM request r WHERE r.description = 'demo_Sur planta transit' LIMIT 1;
INSERT INTO request_assignment (request_id, user_id)
SELECT r.id, 2002 FROM request r WHERE r.description = 'demo_Sur planta done' LIMIT 1;
INSERT INTO request_assignment (request_id, user_id)
SELECT r.id, 2002 FROM request r WHERE r.description = 'demo_Centro despacho' LIMIT 1;
INSERT INTO request_assignment (request_id, user_id)
SELECT r.id, 2003 FROM request r WHERE r.description = 'demo_Centro despacho 2' LIMIT 1;

-- Historial con ratings para RATED
INSERT INTO request_history (request_id, request_status, user_id, rating, comments, create_date)
SELECT r.id, 'RATED', 2001, 5, 'demo Excelente', NOW() - INTERVAL 10 DAY FROM request r WHERE r.description = 'demo_Valorada 1' LIMIT 1;
INSERT INTO request_history (request_id, request_status, user_id, rating, comments, create_date)
SELECT r.id, 'RATED', 2001, 4, 'demo Muy bien', NOW() - INTERVAL 12 DAY FROM request r WHERE r.description = 'demo_Valorada 2' LIMIT 1;
INSERT INTO request_history (request_id, request_status, user_id, rating, comments, create_date)
SELECT r.id, 'RATED', 2001, 5, 'demo Perfecto', NOW() - INTERVAL 15 DAY FROM request r WHERE r.description = 'demo_Valorada 3' LIMIT 1;
INSERT INTO request_history (request_id, request_status, user_id, rating, comments, create_date)
SELECT r.id, 'RATED', 2004, 4, 'demo Bien', NOW() - INTERVAL 5 DAY FROM request r WHERE r.description = 'demo_Norte valorada' LIMIT 1;
INSERT INTO request_history (request_id, request_status, user_id, rating, comments, create_date)
SELECT r.id, 'RATED', 2004, 5, 'demo Genial', NOW() - INTERVAL 8 DAY FROM request r WHERE r.description = 'demo_Sucursal A valorada' LIMIT 1;
INSERT INTO request_history (request_id, request_status, user_id, rating, comments, create_date)
SELECT r.id, 'RATED', 2001, 5, 'demo Ok', NOW() - INTERVAL 7 DAY FROM request r WHERE r.description = 'demo_Sur oficina valorada' LIMIT 1;
INSERT INTO request_history (request_id, request_status, user_id, rating, comments, create_date)
SELECT r.id, 'RATED', 2001, 4, 'demo', NOW() - INTERVAL 3 DAY FROM request r WHERE r.description = 'demo_Centro despacho' LIMIT 1;
