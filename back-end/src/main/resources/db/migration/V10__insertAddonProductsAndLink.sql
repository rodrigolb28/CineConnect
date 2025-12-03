-- Insert Addon products
INSERT INTO products (name, price, type_id, image_url)
SELECT 'Butter', 2.00, id, '/uploads/butter.png' FROM product_types WHERE name = 'Addon';

INSERT INTO products (name, price, type_id, image_url)
SELECT 'Salt', 0.50, id, '/uploads/salt.png' FROM product_types WHERE name = 'Addon';

-- Link new Addons to Food products
INSERT INTO product_addons (product_id, addon_id)
SELECT p.id, a.id
FROM products p
CROSS JOIN products a
JOIN product_types pt_p ON p.type_id = pt_p.id
JOIN product_types pt_a ON a.type_id = pt_a.id
WHERE pt_p.name = 'Food' AND pt_a.name = 'Addon';
