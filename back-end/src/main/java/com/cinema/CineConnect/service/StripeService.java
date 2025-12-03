package com.cinema.CineConnect.service;

import com.cinema.CineConnect.model.DTO.ProductRecord;
import com.cinema.CineConnect.model.Product;
import com.cinema.CineConnect.model.Purchase;
import com.cinema.CineConnect.model.PurchaseItem;
import com.cinema.CineConnect.model.factory.ProductFactory;
import com.cinema.CineConnect.repository.PurchaseRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class StripeService {

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    private final PurchaseRepository purchaseRepository;

    public StripeService(PurchaseRepository purchaseRepository) {
        this.purchaseRepository = purchaseRepository;
    }

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    public com.stripe.model.PaymentIntent createPaymentIntent(List<ProductRecord> cart, UUID userId)
            throws StripeException {
        long totalAmount = 0;
        Purchase purchase = new Purchase(userId, null, BigDecimal.ZERO, "PENDING");

        for (ProductRecord productRecord : cart) {
            Product product = ProductFactory.createProduct(productRecord);
            int quantity = productRecord.quantity();
            long itemAmount = product.getPrice().multiply(new BigDecimal(100)).longValue() * quantity;
            totalAmount += itemAmount;

            PurchaseItem item = new PurchaseItem();
            item.setProductId(productRecord.productId());
            item.setQuantity(quantity);
            item.setPriceAtPurchase(product.getPrice());

            List<PurchaseItem> addOns = new ArrayList<>();
            for (Product addOn : product.getInfo()) {
                long addOnAmount = addOn.getPrice().multiply(new BigDecimal(100)).longValue();
                totalAmount += addOnAmount;

                PurchaseItem addOnItem = new PurchaseItem();
                addOnItem.setProductId(addOn.getId());
                addOnItem.setQuantity(1);
                addOnItem.setPriceAtPurchase(addOn.getPrice());
                addOns.add(addOnItem);
            }
            item.setAddons(addOns);
            purchase.addItem(item);
        }

        purchase.setTotalAmount(BigDecimal.valueOf(totalAmount).divide(BigDecimal.valueOf(100)));
        UUID purchaseId = purchaseRepository.save(purchase);

        Map<String, String> metadata = new HashMap<>();
        metadata.put("purchaseId", purchaseId.toString());
        metadata.put("userId", userId.toString());

        com.stripe.param.PaymentIntentCreateParams params = com.stripe.param.PaymentIntentCreateParams.builder()
                .setAmount(totalAmount)
                .setCurrency("brl")
                .putAllMetadata(metadata)
                .setAutomaticPaymentMethods(
                        com.stripe.param.PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                .setEnabled(true)
                                .setAllowRedirects(
                                        com.stripe.param.PaymentIntentCreateParams.AutomaticPaymentMethods.AllowRedirects.NEVER)
                                .build())
                .build();

        com.stripe.model.PaymentIntent paymentIntent = com.stripe.model.PaymentIntent.create(params);

        // Update purchase with payment ID (PaymentIntent ID)
        purchase.setPaymentId(paymentIntent.getId());
        purchaseRepository.updatePaymentId(purchaseId, paymentIntent.getId());

        return paymentIntent;
    }

    public com.stripe.model.PaymentIntent retrievePaymentIntent(String paymentIntentId) throws StripeException {
        return com.stripe.model.PaymentIntent.retrieve(paymentIntentId);
    }
}
