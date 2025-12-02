package com.cinema.CineConnect.model.DTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PurchaseDetailsRecord(
        UUID purchaseId,
        UUID userId,
        String paymentId,
        BigDecimal totalAmount,
        String status,
        LocalDateTime createdAt,

        UUID itemId,
        UUID itemProductId,
        String itemProductName,
        String itemProductImage,
        Integer itemQuantity,
        BigDecimal itemPriceAtPurchase,

        UUID addonId,
        UUID addonProductId,
        String addonProductName,
        String addonProductImage,
        Integer addonQuantity,
        BigDecimal addonPriceAtPurchase) {
}
