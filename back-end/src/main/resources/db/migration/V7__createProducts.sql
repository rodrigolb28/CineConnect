-- Enable the pgcrypto extension for UUID generation
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Product Types table
CREATE TABLE IF NOT EXISTS product_types (
                                             id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                             name VARCHAR(255) NOT NULL,
                                             description VARCHAR(255)
);

-- Products table
CREATE TABLE IF NOT EXISTS products (
                                        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                        name VARCHAR(255) NOT NULL,
                                        price NUMERIC(10,2) NOT NULL,
                                        type_id UUID,
                                        quantity INT NOT NULL DEFAULT 100,
                                        available BOOLEAN NOT NULL DEFAULT TRUE,
                                        image_url VARCHAR(500),
                                        created_at TIMESTAMP DEFAULT NOW(),

                                        CONSTRAINT fk_products_type
                                            FOREIGN KEY (type_id)
                                                REFERENCES product_types(id)
                                                ON DELETE SET NULL
);

-- Carts table
CREATE TABLE IF NOT EXISTS carts (
                                     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                     name VARCHAR(255) NOT NULL,
                                     price NUMERIC(10,2) NOT NULL,
                                     user_id UUID,
                                     created_at TIMESTAMP DEFAULT NOW(),

                                     CONSTRAINT fk_carts_user
                                         FOREIGN KEY (user_id)
                                             REFERENCES users(id)
                                             ON DELETE CASCADE
);

-- Cart Items table
CREATE TABLE IF NOT EXISTS cart_items (
                                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                          cart_id UUID NOT NULL,
                                          product_id UUID NOT NULL,
                                          quantity INT NOT NULL DEFAULT 1,
                                          price_at_time NUMERIC(10,2) NOT NULL,

                                          CONSTRAINT fk_cart_items_cart
                                              FOREIGN KEY (cart_id)
                                                  REFERENCES carts(id)
                                                  ON DELETE CASCADE,

                                          CONSTRAINT fk_cart_items_product
                                              FOREIGN KEY (product_id)
                                                  REFERENCES products(id)
                                                  ON DELETE CASCADE
);

-- Addons table
CREATE TABLE IF NOT EXISTS cart_addons (
                                      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                      cart_item_id UUID NOT NULL,
                                      product_id UUID NOT NULL,
                                      quantity INT NOT NULL DEFAULT 1,
                                      price_at_time NUMERIC(10,2) NOT NULL,

                                      CONSTRAINT fk_addons_cart_item
                                          FOREIGN KEY (cart_item_id)
                                              REFERENCES cart_items(id)
                                              ON DELETE CASCADE,

                                      CONSTRAINT fk_addons_product
                                          FOREIGN KEY (product_id)
                                              REFERENCES products(id)
                                              ON DELETE CASCADE
);
INSERT INTO product_types (id, name, description) VALUES
                                                      (gen_random_uuid(), 'Food', 'Snacks and edible products'),
                                                      (gen_random_uuid(), 'Drink', 'Cold or hot beverages'),
                                                      (gen_random_uuid(), 'Combo', 'Combination of food and drinks'),
                                                      (gen_random_uuid(), 'Ticket', 'Movie entry tickets'),

                                                      (gen_random_uuid(), 'Addon', 'Extra toppings or add-ons');

INSERT INTO products (name, price, type_id, image_url)
SELECT 'Large Popcorn', 25.00, id, '/uploads/pop_big.png' FROM product_types WHERE name = 'Food';

INSERT INTO products (name, price, type_id, image_url)
SELECT 'Medium Popcorn', 20.00, id, '/uploads/pop_medium.png' FROM product_types WHERE name = 'Food';

INSERT INTO products (name, price, type_id, image_url)
SELECT 'Chocolate Bar', 12.00, id, '/uploads/chocolate_bar.png' FROM product_types WHERE name = 'Food';

INSERT INTO products (name, price, type_id, image_url)
SELECT 'Large Soda', 18.00, id, '/uploads/soda.png' FROM product_types WHERE name = 'Drink';

INSERT INTO products (name, price, type_id, image_url)
SELECT 'Water Bottle', 8.00, id, '/uploads/water_bottle.png' FROM product_types WHERE name = 'Drink';
