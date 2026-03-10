-- Categorías y subcategorías de servicio para formulario de solicitudes y tests E2E.
-- Sin esto, service_category está vacía y el formulario New Request no tiene opciones.

INSERT INTO service_category (id, name, description) VALUES
(1, 'Mantenimiento', 'Servicios de mantenimiento general'),
(2, 'Limpieza', 'Servicios de limpieza')
ON DUPLICATE KEY UPDATE id = id;

INSERT INTO service_sub_category (id, name, description, service_category_id) VALUES
(1, 'Mantenimiento eléctrico', 'Reparaciones e instalaciones eléctricas', 1),
(2, 'Mantenimiento HVAC', 'Aire acondicionado y calefacción', 1),
(3, 'Limpieza general', 'Limpieza de oficinas y espacios', 2)
ON DUPLICATE KEY UPDATE id = id;
