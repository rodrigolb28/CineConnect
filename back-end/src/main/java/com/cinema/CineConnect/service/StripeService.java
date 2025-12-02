package com.cinema.CineConnect.service;

import com.cinema.CineConnect.model.DTO.ProductRecord;
import com.cinema.CineConnect.model.Product;
import com.cinema.CineConnect.model.Purchase;
import com.cinema.CineConnect.model.PurchaseItem;
import com.cinema.CineConnect.model.factory.ProductFactory;
import com.cinema.CineConnect.repository.PurchaseRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
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

    public String createCheckoutSession(List<ProductRecord> cart, String successUrl, String cancelUrl, UUID userId) {
        List<SessionCreateParams.LineItem> lineItems = new ArrayList<>();
        long totalAmount = 0;

        // 1. Build Line Items and Calculate Total
        for (ProductRecord productRecord : cart) {
            Product product = ProductFactory.createProduct(productRecord);
            long itemAmount = product.getPrice().multiply(new BigDecimal(100)).longValue();
            totalAmount += itemAmount;

            // Item principal
            lineItems.add(buildLineItem(product));

            // Add-ons via polymorphism
            for (Product addOn : product.getInfo()) {
                long addOnAmount = addOn.getPrice().multiply(new BigDecimal(100)).longValue();
                totalAmount += addOnAmount;
                lineItems.add(buildLineItem(addOn, "Add-on: "));
            }
        }

        // 2. Create Pending Purchase
        Purchase purchase = new Purchase(userId, null, BigDecimal.valueOf(totalAmount).divide(BigDecimal.valueOf(100)),
                "PENDING");

        // Populate Purchase Items (similar to createPaymentIntent)
        for (ProductRecord productRecord : cart) {
            Product product = ProductFactory.createProduct(productRecord);

            PurchaseItem item = new PurchaseItem();
            item.setProductId(productRecord.productId());
            item.setQuantity(1);
            item.setPriceAtPurchase(product.getPrice());

            List<PurchaseItem> addOns = new ArrayList<>();
            for (Product addOn : product.getInfo()) {
                PurchaseItem addOnItem = new PurchaseItem();
                addOnItem.setProductId(addOn.getId());
                addOnItem.setQuantity(1);
                addOnItem.setPriceAtPurchase(addOn.getPrice());
                addOns.add(addOnItem);
            }
            item.setAddons(addOns);
            purchase.addItem(item);
        }

        UUID purchaseId = purchaseRepository.save(purchase);

        // 3. Create Stripe Session
        SessionCreateParams params = SessionCreateParams.builder()
                .addAllLineItem(lineItems)
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl + "&session_id={CHECKOUT_SESSION_ID}") // Append session_id to success URL
                .setCancelUrl(cancelUrl)
                .putMetadata("purchaseId", purchaseId.toString())
                .putMetadata("userId", userId.toString())
                .build();

        try {
            Session session = Session.create(params);

            // 4. Update Purchase with Session ID (as paymentId)
            purchaseRepository.updatePaymentId(purchaseId, session.getId());

            return session.getUrl(); // URL do Stripe Checkout
        } catch (StripeException e) {
            throw new RuntimeException("Erro ao criar Stripe Checkout Session", e);
        }
    }

    private SessionCreateParams.LineItem buildLineItem(Product product) {
        return buildLineItem(product, "");
    }

    private SessionCreateParams.LineItem buildLineItem(Product product, String prefixName) {
        return SessionCreateParams.LineItem.builder()
                .setPriceData(
                        SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency("brl")
                                .setUnitAmount(product.getPrice().multiply(new BigDecimal(100)).longValue())
                                .setProductData(
                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                .setName(prefixName + product.getName())
                                                .build())
                                .build())
                .setQuantity(1L)
                .build();
    }

    public com.stripe.model.PaymentIntent createPaymentIntent(List<ProductRecord> cart, UUID userId)
            throws StripeException {
        long totalAmount = 0;
        Purchase purchase = new Purchase(userId, null, BigDecimal.ZERO, "PENDING");

        for (ProductRecord productRecord : cart) {
            Product product = ProductFactory.createProduct(productRecord);
            long itemAmount = product.getPrice().multiply(new BigDecimal(100)).longValue();
            totalAmount += itemAmount;

            PurchaseItem item = new PurchaseItem();
            item.setProductId(productRecord.productId());
            item.setQuantity(1); // Assuming 1 for now based on cart structure
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
