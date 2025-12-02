package com.cinema.CineConnect.repository;

import com.cinema.CineConnect.model.DTO.ProductRecord;
import com.cinema.CineConnect.model.DTO.TypeCount;
import com.cinema.CineConnect.model.Product;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class ProductRepository {

    private final JdbcClient jdbcClient;

    public ProductRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public void removeProduct(UUID id) {
        jdbcClient.sql("""
                    DELETE FROM products
                    WHERE id = :id
                """)
                .param("id", id)
                .update();
    }

    public boolean removeProductTypeSafely(UUID typeId) {
        // check if any product uses this type
        Integer count = jdbcClient.sql("""
                    SELECT COUNT(*)
                    FROM products
                    WHERE type_id = :id
                """)
                .param("id", typeId)
                .query(Integer.class)
                .single();

        if (count > 0)
            return false; // refuse to delete

        // delete type
        jdbcClient.sql("""
                    DELETE FROM product_types
                    WHERE id = :id
                """)
                .param("id", typeId)
                .update();

        return true;
    }

    private List<ProductRecord> getAddonsForProduct(UUID productId) {
        return jdbcClient.sql("""
                    SELECT p.id as productId, p.name, pt.name as type, p.price, image_url, p.quantity, p.available,
                           CAST(NULL AS BIGINT) as sessionId,
                           CAST(NULL AS varchar) as seatNumber,
                           CAST(NULL AS varchar) as addOns
                    FROM product_addons pa
                    JOIN products p ON pa.addon_id = p.id
                    JOIN product_types pt ON p.type_id = pt.id
                    WHERE pa.product_id = :productId
                """)
                .param("productId", productId)
                .query(ProductRecord.class)
                .list();
    }

    public List<ProductRecord> findAll() {
        List<ProductRecord> products = jdbcClient.sql("""
                    SELECT p.id as productId, p.name, pt.name as type, p.price, image_url, p.quantity, p.available,
                           p.session_id as sessionId,
                           p.seat_number as seatNumber,
                           CAST(NULL AS varchar) as addOns
                    FROM products p
                    JOIN product_types pt ON p.type_id = pt.id
                    WHERE pt.name <> 'Ticket'
                """)
                .query(ProductRecord.class)
                .list();

        return products.stream().map(p -> new ProductRecord(
                p.productId(), p.name(), p.type(), p.price(), p.quantity(), p.available(), p.sessionId(), p.imageUrl(),
                getAddonsForProduct(p.productId()), p.seatNumber())).toList();
    }

    public Integer getQuantityById(UUID productId) {
        return jdbcClient.sql("""
                    SELECT quantity
                    FROM products
                    WHERE id = :productId
                """)
                .param("productId", productId)
                .query(Integer.class)
                .single();
    }

    public void updateProductQuantity(UUID productId, int newQuantity) {
        boolean available = newQuantity > 0;
        jdbcClient.sql("""
                    UPDATE products
                    SET quantity = :quantity, available = :available
                    WHERE id = :id
                """)
                .param("quantity", newQuantity)
                .param("available", available)
                .param("id", productId)
                .update();
    }

    public List<Product> findProductsByType(UUID productTypeId) {
        return jdbcClient.sql("""
                    SELECT * FROM products WHERE type_id = :productType
                """)
                .param("productType", productTypeId)
                .query(Product.class)
                .list();
    }

    public List<ProductRecord> findProductsByType(String typeName) {
        List<ProductRecord> products = jdbcClient.sql("""
                    SELECT p.id as productId, p.name, pt.name as type, p.price, image_url, p.quantity, p.available,
                           p.session_id as sessionId,
                           p.seat_number as seatNumber,
                           CAST(NULL AS varchar) as addOns
                    FROM products p
                    JOIN product_types pt ON p.type_id = pt.id
                    WHERE pt.name = :typeName
                """)
                .param("typeName", typeName)
                .query(ProductRecord.class)
                .list();

        return products.stream().map(p -> new ProductRecord(
                p.productId(), p.name(), p.type(), p.price(), p.quantity(), p.available(), p.sessionId(), p.imageUrl(),
                getAddonsForProduct(p.productId()), p.seatNumber())).toList();
    }

    public ProductRecord findById(UUID id) {
        ProductRecord p = jdbcClient.sql("""
                    SELECT p.id as productId, p.name, pt.name as type, p.price, image_url, p.quantity, p.available,
                           p.session_id as sessionId,
                           p.seat_number as seatNumber,
                           CAST(NULL AS varchar) as addOns
                    FROM products p
                    JOIN product_types pt ON p.type_id = pt.id
                    WHERE p.id = :id
                """)
                .param("id", id)
                .query(ProductRecord.class)
                .single();

        return new ProductRecord(
                p.productId(), p.name(), p.type(), p.price(), p.quantity(), p.available(), p.sessionId(), p.imageUrl(),
                getAddonsForProduct(p.productId()), p.seatNumber());
    }

    public void saveProduct(Product product) {
        // First get type_id
        UUID typeId = jdbcClient.sql("SELECT id FROM product_types WHERE name = :type")
                .param("type", product.getType())
                .query(UUID.class)
                .single();

        Long sessionId = null;
        String seatNumber = null;

        if (product instanceof com.cinema.CineConnect.model.Ticket ticket) {
            sessionId = ticket.getSessionId();
            seatNumber = ticket.getSeatNumber();
        }

        jdbcClient
                .sql("""
                            INSERT INTO products (id, name, type_id, price, quantity, available, image_url, session_id, seat_number)
                            VALUES (:id, :name, :typeId, :price, :quantity, :available, :imageUrl, :sessionId, :seatNumber)
                        """)
                .param("id", product.getId())
                .param("name", product.getName())
                .param("typeId", typeId)
                .param("price", product.getPrice())
                .param("quantity", product.getQuantity())
                .param("available", product.isAvailable())
                .param("imageUrl", product.getImageUrl())
                .param("sessionId", sessionId)
                .param("seatNumber", seatNumber)
                .update();
    }

    public List<TypeCount> listTypesWithCount() {
        return jdbcClient.sql("""
                    SELECT pt.id, pt.name, COUNT(p.id) AS total
                    FROM product_types pt
                    LEFT JOIN products p ON p.type_id = pt.id
                    GROUP BY pt.id, pt.name
                """)
                .query(TypeCount.class)
                .list();
    }

}
