-- Perfil Worker (4)
INSERT INTO profile (id, description) VALUES (4, 'Worker');

-- Menú: type H = grupo/header, N = enlace normal. URI sin context path (SPA paths).
INSERT INTO menu_item (id, description, uri, position, type) VALUES
(1, 'Principal', '', 1, 'H'),
(2, 'Home', '/', 2, 'N'),
(3, 'Requests', '/requests', 3, 'N'),
(4, 'New Request', '/requests/new', 4, 'N'),
(5, 'Catálogos', '', 5, 'H'),
(6, 'Companies', '/companies', 6, 'N'),
(7, 'Sites', '/sites', 7, 'N'),
(8, 'Service categories', '/service-categories', 8, 'N'),
(9, 'Service subcategories', '/service-sub-categories', 9, 'N'),
(10, 'Admin', '', 10, 'H'),
(11, 'Profiles', '/admin/profiles', 11, 'N'),
(12, 'Persons', '/admin/customers', 12, 'N'),
(13, 'Users', '/admin/users', 13, 'N');

-- SuperAdmin (1): acceso a todo
INSERT INTO profile_menu_item (profile_id, item_menu_id)
SELECT 1, id FROM menu_item;

-- Requester (2): Principal + Home, Requests, New Request
INSERT INTO profile_menu_item (profile_id, item_menu_id) VALUES
(2, 1), (2, 2), (2, 3), (2, 4);

-- Company Admin (3): Requester + Catálogos + Admin (Persons, Users; sin Profiles)
INSERT INTO profile_menu_item (profile_id, item_menu_id) VALUES
(3, 1), (3, 2), (3, 3), (3, 4),
(3, 5), (3, 6), (3, 7), (3, 8), (3, 9),
(3, 10), (3, 12), (3, 13);

-- Worker (4): Principal + Home, Requests (solo ver asignados)
INSERT INTO profile_menu_item (profile_id, item_menu_id) VALUES
(4, 1), (4, 2), (4, 3);
