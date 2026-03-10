-- Company Admin (3) no debe tener acceso a Catálogos ni Admin.
-- Se eliminan: cabecera Catálogos (5), Sites (7), cabecera Admin (10), Persons (12), Users (13).
-- Tras esta migración, Company Admin solo verá: Principal (Home, Requests, New Request).
DELETE FROM profile_menu_item
WHERE profile_id = 3 AND item_menu_id IN (5, 7, 10, 12, 13);
