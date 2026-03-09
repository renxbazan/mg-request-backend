-- Datos iniciales para pruebas
-- Perfiles: 1=Super Admin, 2=Requester, 3=Company Admin
INSERT INTO profile (id, description) VALUES (1, 'Super Admin');
INSERT INTO profile (id, description) VALUES (2, 'Requester');
INSERT INTO profile (id, description) VALUES (3, 'Company Admin');

-- Empresa de ejemplo
INSERT INTO company (id, name, description, company_type) VALUES (1, 'MG Services Unlimited', 'Empresa de servicios generales', 'COMPANY');

-- Cliente/contacto (vinculado a la empresa)
INSERT INTO customer (id, first_name, last_name, email, company_id, employee) VALUES (1, 'Admin', 'Sistema', 'admin@mgservices.com', 1, TRUE);

-- Sitio de ejemplo
INSERT INTO site (id, name, description, company_id) VALUES (1, 'Oficina Central', 'Sede principal', 1);

-- Usuario para pruebas: usuario "admin", contraseña "password"
-- Hash BCrypt válido para "password" (compatible con Spring Security)
INSERT INTO users (id, username, password, customer_id, profile_id, site_id) VALUES
(1, 'admin', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 1, 1, 1);
