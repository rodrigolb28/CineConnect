package com.cinema.CineConnect.repository;

import com.cinema.CineConnect.model.DTO.PurchaseDetailsRecord;
import com.cinema.CineConnect.model.Purchase;
import com.cinema.CineConnect.model.PurchaseItem;
import com.cinema.CineConnect.service.BuildItemListService;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
public class PurchaseRepository {

    private final JdbcClient jdbcClient;

    public PurchaseRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Transactional
    public UUID save(Purchase purchase) {
        // 1. Insert Purchase
        UUID purchaseId = jdbcClient.sql("""
                    INSERT INTO purchases (user_id, payment_id, total_amount, status, created_at)
                    VALUES (:userId, :paymentId, :totalAmount, :status, :createdAt)
                    RETURNING id
                """)
                .param("userId", purchase.getUserId())
                .param("paymentId", purchase.getPaymentId())
                .param("totalAmount", purchase.getTotalAmount())
                .param("status", purchase.getStatus())
                .param("createdAt", purchase.getCreatedAt())
                .query(UUID.class)
                .single();

        purchase.setId(purchaseId);
        System.out.println("Purchase saved successfully for user: " + purchase.getUserId());
        // 2. Insert Items
        for (PurchaseItem item : purchase.getItems()) {
            item.setPurchaseId(purchaseId);
            saveItem(item);
        }

        return purchaseId;
    }

    private void saveItem(PurchaseItem item) {
        UUID itemId = jdbcClient.sql("""
                    INSERT INTO purchase_items (purchase_id, product_id, quantity, price_at_purchase)
                    VALUES (:purchaseId, :productId, :quantity, :priceAtPurchase)
                    RETURNING id
                """)
                .param("purchaseId", item.getPurchaseId())
                .param("productId", item.getProductId())
                .param("quantity", item.getQuantity())
                .param("priceAtPurchase", item.getPriceAtPurchase())
                .query(UUID.class)
                .single();

        // 3. Insert Addons
        if (item.getAddons() != null) {
            for (PurchaseItem addon : item.getAddons()) {
                jdbcClient.sql("""
                            INSERT INTO purchase_addons (purchase_item_id, product_id, quantity, price_at_purchase)
                            VALUES (:purchaseItemId, :productId, :quantity, :priceAtPurchase)
                        """)
                        .param("purchaseItemId", itemId)
                        .param("productId", addon.getProductId())
                        .param("quantity", addon.getQuantity())
                        .param("priceAtPurchase", addon.getPriceAtPurchase())
                        .update();
            }
        }
    }

    public void updateStatus(UUID purchaseId, String status) {
        jdbcClient.sql("UPDATE purchases SET status = :status WHERE id = :id")
                .param("status", status)
                .param("id", purchaseId)
                .update();
    }

    public void updatePaymentId(UUID purchaseId, String paymentId) {
        jdbcClient.sql("UPDATE purchases SET payment_id = :paymentId WHERE id = :id")
                .param("paymentId", paymentId)
                .param("id", purchaseId)
                .update();
    }

    public List<Purchase> retrieveUserPurchases(UUID userId) {
        // Creates table with all items that were purchased by the user and which ones
        List<PurchaseDetailsRecord> records = jdbcClient.sql("""
                    SELECT
                        p.id AS purchaseId,
                        p.user_id AS userId,
                        p.payment_id AS paymentId,
                        p.total_amount AS totalAmount,
                        p.status AS status,
                        p.created_at AS createdAt,

                        i.id AS itemId,
                        i.product_id AS itemProductId,
                        pi.name AS itemProductName,
                        pi.image_url AS itemProductImage,
                        i.quantity AS itemQuantity,
                        i.price_at_purchase AS itemPriceAtPurchase,

                        a.id AS addonId,
                        a.product_id AS addonProductId,
                        pa.name AS addonProductName,
                        pa.image_url AS addonProductImage,
                        a.quantity AS addonQuantity,
                        a.price_at_purchase AS addonPriceAtPurchase

                    FROM purchases p
                    LEFT JOIN purchase_items i ON i.purchase_id = p.id
                    LEFT JOIN products pi ON pi.id = i.product_id
                    LEFT JOIN purchase_addons a ON a.purchase_item_id = i.id
                    LEFT JOIN products pa ON pa.id = a.product_id
                    WHERE p.user_id = :userId
                    ORDER BY p.created_at DESC, i.id, a.id
                """)
                .param("userId", userId)
                .query(PurchaseDetailsRecord.class)
                .list();

        return BuildItemListService.buildListPurchases(records);
    }

    public List<PurchaseDetailsRecord> retrievePurchaseData(UUID userId, UUID purchaseId) {
        return jdbcClient.sql("""
                    SELECT
                      p.id AS purchase_id,
                      p.user_id,
                      p.payment_id,
                      p.total_amount,
                      p.status,
                      p.created_at,

                      i.id AS item_id,
                      i.product_id AS item_product_id,
                      pi.name AS item_product_name,
                      pi.image_url AS item_product_image,
                      i.quantity AS item_quantity,
                      i.price_at_purchase AS item_price,

                      a.id AS addon_id,
                      a.product_id AS addon_product_id,
                      pa.name AS addon_product_name,
                      pa.image_url AS addon_product_image,
                      a.quantity AS addon_quantity,
                      a.price_at_purchase AS addon_price
                  FROM purchases p
                  LEFT JOIN purchase_items i ON i.purchase_id = p.id
                  LEFT JOIN products pi ON pi.id = i.product_id
                  LEFT JOIN purchase_addons a ON a.purchase_item_id = i.id
                  LEFT JOIN products pa ON pa.id = a.product_id
                  WHERE p.id = :purchaseId
                  AND p.user_id = :userId;
                """)
                .param("purchaseId", purchaseId)
                .param("userId", userId)
                .query(PurchaseDetailsRecord.class)
                .list();
    }

    public Purchase retrievePurchaseById(UUID purchaseId) {
        List<PurchaseDetailsRecord> records = jdbcClient.sql("""
                    SELECT
                        p.id AS purchaseId,
                        p.user_id AS userId,
                        p.payment_id AS paymentId,
                        p.total_amount AS totalAmount,
                        p.status AS status,
                        p.created_at AS createdAt,

                        i.id AS itemId,
                        i.product_id AS itemProductId,
                        pi.name AS itemProductName,
                        pi.image_url AS itemProductImage,
                        i.quantity AS itemQuantity,
                        i.price_at_purchase AS itemPriceAtPurchase,

                        a.id AS addonId,
                        a.product_id AS addonProductId,
                        pa.name AS addonProductName,
                        pa.image_url AS addonProductImage,
                        a.quantity AS addonQuantity,
                        a.price_at_purchase AS addonPriceAtPurchase

                    FROM purchases p
                    LEFT JOIN purchase_items i ON i.purchase_id = p.id
                    LEFT JOIN products pi ON pi.id = i.product_id
                    LEFT JOIN purchase_addons a ON a.purchase_item_id = i.id
                    LEFT JOIN products pa ON pa.id = a.product_id
                    WHERE p.id = :purchaseId
                """)
                .param("purchaseId", purchaseId)
                .query(PurchaseDetailsRecord.class)
                .list();

        List<Purchase> purchases = BuildItemListService.buildListPurchases(records);
        return purchases.isEmpty() ? null : purchases.get(0);
    }

    public Purchase retrievePurchaseByPaymentId(String paymentId) {
        List<PurchaseDetailsRecord> records = jdbcClient.sql("""
                    SELECT
                        p.id AS purchaseId,
                        p.user_id AS userId,
                        p.payment_id AS paymentId,
                        p.total_amount AS totalAmount,
                        p.status AS status,
                        p.created_at AS createdAt,

                        i.id AS itemId,
                        i.product_id AS itemProductId,
                        pi.name AS itemProductName,
                        pi.image_url AS itemProductImage,
                        i.quantity AS itemQuantity,
                        i.price_at_purchase AS itemPriceAtPurchase,

                        a.id AS addonId,
                        a.product_id AS addonProductId,
                        pa.name AS addonProductName,
                        pa.image_url AS addonProductImage,
                        a.quantity AS addonQuantity,
                        a.price_at_purchase AS addonPriceAtPurchase

                    FROM purchases p
                    LEFT JOIN purchase_items i ON i.purchase_id = p.id
                    LEFT JOIN products pi ON pi.id = i.product_id
                    LEFT JOIN purchase_addons a ON a.purchase_item_id = i.id
                    LEFT JOIN products pa ON pa.id = a.product_id
                    WHERE p.payment_id = :paymentId
                """)
                .param("paymentId", paymentId)
                .query(PurchaseDetailsRecord.class)
                .list();

        List<Purchase> purchases = BuildItemListService.buildListPurchases(records);
        return purchases.isEmpty() ? null : purchases.get(0);
    }

}
