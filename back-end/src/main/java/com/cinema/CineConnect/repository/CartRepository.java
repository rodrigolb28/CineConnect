package com.cinema.CineConnect.repository;

import com.cinema.CineConnect.model.Cart;
import com.cinema.CineConnect.model.CartItem;
import com.cinema.CineConnect.model.Product;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class CartRepository {
    JdbcClient jdbcClient;

    CartRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    private void createAddon(UUID cartItemId, Product addon) {
        jdbcClient.sql("""
                    INSERT INTO cart_addons (cart_item_id, product_id, quantity, price_at_time)
                    VALUES (:cartItemId, :productId, :quantity, :priceAtTime)
                """)
                .param("cartItemId", cartItemId)
                .param("productId", addon.getId())
                .param("quantity", 1) // default quantity 1, can extend if needed
                .param("priceAtTime", addon.getPrice())
                .update();
    }

    private UUID createCartItem(UUID cartId, CartItem item) {
        // Insert the cart item and get its generated UUID
        UUID cartItemId = jdbcClient.sql("""
                    INSERT INTO cart_items (cart_id, product_id, quantity, price_at_time)
                    VALUES (:cartId, :productId, :quantity, :priceAtTime)
                    RETURNING id
                """)
                .param("cartId", cartId)
                .param("productId", item.getProduct().getId())
                .param("quantity", item.getQuantity())
                .param("priceAtTime", item.getTotalPrice())
                .query(UUID.class)
                .single();

        // Insert all addons for this cart item
        for (Product addon : item.getAddons()) {
            createAddon(cartItemId, addon);
        }

        return cartItemId;
    }

    public UUID createCart(Cart cart) {
        // Insert the cart and get the generated ID
        UUID cartId = jdbcClient.sql("""
                    INSERT INTO carts (user_id, price, name)
                    VALUES (:userId, :price, :name)
                    RETURNING id
                """)
                .param("userId", cart.getUserId())
                .param("price", cart.getPrice())
                .param("name", "Cart of user " + cart.getUserId()) // optional name
                .query(UUID.class)
                .single();

        // Insert items
        for (CartItem item : cart.getItems()) {
            createCartItem(cartId, item);
        }

        return cartId;
    }

    public UUID findByUserId(UUID userId) {
        return jdbcClient.sql("SELECT id FROM carts WHERE user_id = :userId")
                .param("userId", userId)
                .query(UUID.class)
                .optional()
                .orElse(null);
    }

    public UUID addItem(UUID cartId, UUID productId, int quantity, java.math.BigDecimal price) {
        return jdbcClient.sql("""
                INSERT INTO cart_items (cart_id, product_id, quantity, price_at_time)
                VALUES (:cartId, :productId, :quantity, :price)
                RETURNING id
                """)
                .param("cartId", cartId)
                .param("productId", productId)
                .param("quantity", quantity)
                .param("price", price)
                .query(UUID.class)
                .single();
    }

    public void addAddon(UUID cartItemId, UUID productId, int quantity, java.math.BigDecimal price) {
        jdbcClient.sql("""
                INSERT INTO cart_addons (cart_item_id, product_id, quantity, price_at_time)
                VALUES (:cartItemId, :productId, :quantity, :price)
                """)
                .param("cartItemId", cartItemId)
                .param("productId", productId)
                .param("quantity", quantity)
                .param("price", price)
                .update();
    }

    public void updateItemQuantity(UUID cartItemId, int quantity) {
        jdbcClient.sql("UPDATE cart_items SET quantity = :quantity WHERE id = :id")
                .param("quantity", quantity)
                .param("id", cartItemId)
                .update();
    }

    public void removeItem(UUID cartItemId) {
        jdbcClient.sql("DELETE FROM cart_items WHERE id = :id")
                .param("id", cartItemId)
                .update();
    }

    public void clearCart(UUID cartId) {
        jdbcClient.sql("DELETE FROM cart_items WHERE cart_id = :cartId")
                .param("cartId", cartId)
                .update();
    }

    public Cart getCartDetails(UUID cartId) {
        // This is a simplified fetch. Ideally we join tables.
        // For now, let's fetch the cart and then its items.
        // Or we can use a ResultSetExtractor for a single query.
        // Let's stick to simple queries for now to match existing style.

        Cart cart = jdbcClient.sql("SELECT * FROM carts WHERE id = :id")
                .param("id", cartId)
                .query(Cart.class)
                .single();

        java.util.List<CartItem> items = jdbcClient
                .sql("""
                        SELECT ci.*, p.name as product_name, p.price as product_price, p.image_url as product_image, pt.name as product_type
                        FROM cart_items ci
                        JOIN products p ON ci.product_id = p.id
                        JOIN product_types pt ON p.type_id = pt.id
                        WHERE ci.cart_id = :cartId
                        """)
                .param("cartId", cartId)
                .query((rs, rowNum) -> {
                    CartItem item = new CartItem();
                    item.setId(UUID.fromString(rs.getString("id")));
                    item.setProductId(UUID.fromString(rs.getString("product_id")));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setPriceAtTime(rs.getBigDecimal("price_at_time"));

                    // Reconstruct Product object partially for display
                    Product p = new com.cinema.CineConnect.model.FoodProduct(
                            UUID.fromString(rs.getString("product_id")),
                            rs.getString("product_name"),
                            rs.getBigDecimal("product_price"),
                            rs.getString("product_type"),
                            0, // Quantity unknown
                            true, // Available unknown
                            rs.getString("product_image"));
                    item.setProduct(p);
                    return item;
                })
                .list();

        // Fetch addons for each item
        for (CartItem item : items) {
            java.util.List<Product> addons = jdbcClient.sql("""
                    SELECT a.*, p.name, p.price, p.image_url
                    FROM cart_addons a
                    JOIN products p ON a.product_id = p.id
                    WHERE a.cart_item_id = :itemId
                    """)
                    .param("itemId", item.getId())
                    .query((rs, rowNum) -> {
                        return (Product) new com.cinema.CineConnect.model.FoodProduct(
                                UUID.fromString(rs.getString("product_id")),
                                rs.getString("name"),
                                rs.getBigDecimal("price"),
                                "Addon",
                                0,
                                true,
                                rs.getString("image_url"));
                    })
                    .list();
            item.setAddons(addons);
        }

        cart.setItems(items);
        return cart;
    }

    public void updateCartPrice(UUID cartId) {
        jdbcClient.sql("""
                WITH ItemTotal AS (
                    SELECT COALESCE(SUM(quantity * price_at_time), 0) as total
                    FROM cart_items
                    WHERE cart_id = :cartId
                ),
                AddonTotal AS (
                    SELECT COALESCE(SUM(ca.quantity * ca.price_at_time), 0) as total
                    FROM cart_addons ca
                    JOIN cart_items ci ON ca.cart_item_id = ci.id
                    WHERE ci.cart_id = :cartId
                )
                UPDATE carts
                SET price = (SELECT total FROM ItemTotal) + (SELECT total FROM AddonTotal)
                WHERE id = :cartId
                """)
                .param("cartId", cartId)
                .update();
    }

    public boolean isSeatInCart(UUID cartId, Long sessionId, String seatNumber) {
        Integer count = jdbcClient.sql("""
                SELECT COUNT(*)
                FROM cart_items ci
                JOIN products p ON ci.product_id = p.id
                WHERE ci.cart_id = :cartId
                AND p.session_id = :sessionId
                AND p.seat_number = :seatNumber
                """)
                .param("cartId", cartId)
                .param("sessionId", sessionId)
                .param("seatNumber", seatNumber)
                .query(Integer.class)
                .single();
        return count != null && count > 0;
    }

    public CartItem getItem(UUID cartItemId) {
        return jdbcClient
                .sql("""
                        SELECT ci.*, p.name as product_name, p.price as product_price, p.image_url as product_image, pt.name as product_type,
                               p.session_id, p.seat_number
                        FROM cart_items ci
                        JOIN products p ON ci.product_id = p.id
                        JOIN product_types pt ON p.type_id = pt.id
                        WHERE ci.id = :id
                        """)
                .param("id", cartItemId)
                .query((rs, rowNum) -> {
                    CartItem item = new CartItem();
                    item.setId(UUID.fromString(rs.getString("id")));
                    item.setProductId(UUID.fromString(rs.getString("product_id")));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setPriceAtTime(rs.getBigDecimal("price_at_time"));

                    String type = rs.getString("product_type");
                    Product p;
                    if ("Ticket".equalsIgnoreCase(type)) {
                        p = new com.cinema.CineConnect.model.Ticket(
                                UUID.fromString(rs.getString("product_id")),
                                rs.getLong("session_id"),
                                rs.getString("seat_number"),
                                rs.getString("product_name"),
                                type,
                                rs.getBigDecimal("product_price"));
                    } else {
                        p = new com.cinema.CineConnect.model.FoodProduct(
                                UUID.fromString(rs.getString("product_id")),
                                rs.getString("product_name"),
                                rs.getBigDecimal("product_price"),
                                type,
                                0, // Quantity unknown
                                true, // Available unknown
                                rs.getString("product_image"));
                    }
                    item.setProduct(p);
                    return item;
                })
                .single();
    }
}
