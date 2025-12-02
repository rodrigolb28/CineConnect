package com.cinema.CineConnect.service;

import com.cinema.CineConnect.model.DTO.PurchaseDetailsRecord;
import com.cinema.CineConnect.model.Product;
import com.cinema.CineConnect.model.Purchase;
import com.cinema.CineConnect.model.PurchaseItem;

import java.util.*;

public class BuildItemListService {

    public static List<Purchase> buildListPurchases(List<PurchaseDetailsRecord> rows) {

        Map<UUID, Purchase> purchaseMap = new LinkedHashMap<>();

        for (PurchaseDetailsRecord r : rows) {
            // --- PURCHASE ---
            Purchase purchase = purchaseMap.computeIfAbsent(r.purchaseId(), id -> {
                Purchase p = new Purchase();
                p.setId(id);
                p.setUserId(r.userId());
                p.setPaymentId(r.paymentId());
                p.setTotalAmount(r.totalAmount());
                p.setStatus(r.status());
                p.setCreatedAt(r.createdAt());
                return p;
            });

            // No items → continue
            if (r.itemId() == null)
                continue;

            // --- ITEM ---
            // Find existing item or create a new one INSIDE the purchase (no separate map)
            PurchaseItem item = purchase.getItems().stream()
                    .filter(i -> i.getId().equals(r.itemId()))
                    .findFirst()
                    .orElseGet(() -> {
                        PurchaseItem i = new PurchaseItem();
                        i.setId(r.itemId());
                        i.setPurchaseId(r.purchaseId());
                        i.setProductId(r.itemProductId());
                        i.setQuantity(r.itemQuantity());
                        i.setPriceAtPurchase(r.itemPriceAtPurchase());
                        i.setName(r.itemProductName());
                        i.setImageUrl(r.itemProductImage());
                        purchase.getItems().add(i);
                        return i;
                    });

            // --- ADDON ---
            if (r.addonId() != null) {
                PurchaseItem addon = new PurchaseItem();
                addon.setId(r.addonId());
                addon.setProductId(r.addonProductId());
                addon.setQuantity(r.addonQuantity());
                addon.setPriceAtPurchase(r.addonPriceAtPurchase());
                addon.setName(r.addonProductName());
                addon.setImageUrl(r.addonProductImage());
                item.getAddons().add(addon);
            }
        }

        return new ArrayList<>(purchaseMap.values());
    }

}
