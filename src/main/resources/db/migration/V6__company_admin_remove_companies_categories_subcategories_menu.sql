-- Company Admin (3) no debe ver en el menú: Companies (6), Service categories (8), Service subcategories (9).
-- Sigue pudiendo ver Sites (7) y el resto.
DELETE FROM profile_menu_item
WHERE profile_id = 3 AND item_menu_id IN (6, 8, 9);
