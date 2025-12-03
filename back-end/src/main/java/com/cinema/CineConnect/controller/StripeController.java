package com.cinema.CineConnect.controller;

import com.cinema.CineConnect.model.DTO.ProductRecord;
import com.cinema.CineConnect.model.DTO.UserRecordRoleName;
import com.cinema.CineConnect.repository.UserRepository;
import com.cinema.CineConnect.service.StripeService;
import com.cinema.CineConnect.service.TicketService;
import com.stripe.model.PaymentIntent;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/stripe")
public class StripeController {

    private final StripeService stripeService;
    private final TicketService ticketService;
    private final UserRepository userRepository;
    private final com.cinema.CineConnect.service.CartService cartService;

    public StripeController(StripeService stripeService, TicketService ticketService, UserRepository userRepository,
            com.cinema.CineConnect.service.CartService cartService) {
        this.stripeService = stripeService;
        this.ticketService = ticketService;
        this.userRepository = userRepository;
        this.cartService = cartService;
    }

    @PostMapping("/create-payment-intent")
    public ResponseEntity<String> createPaymentIntent(@RequestBody List<ProductRecord> cart) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userIdStr = authentication.getName();
        UserRecordRoleName user = userRepository.findInfoById(UUID.fromString(userIdStr))
                .orElseThrow(() -> new RuntimeException("User not found"));

        try {
            com.stripe.model.PaymentIntent paymentIntent = stripeService.createPaymentIntent(cart,
                    UUID.fromString(user.id()));
            return ResponseEntity.ok(paymentIntent.getClientSecret());
        } catch (com.stripe.exception.StripeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/confirm-payment")
    public ResponseEntity<String> confirmPayment(@RequestBody java.util.Map<String, String> payload) {
        String paymentIntentId = payload.get("paymentIntentId");
        if (paymentIntentId == null) {
            return ResponseEntity.badRequest().body("Missing paymentIntentId");
        }

        try {
            PaymentIntent paymentIntent = stripeService.retrievePaymentIntent(paymentIntentId);
            if ("succeeded".equals(paymentIntent.getStatus())) {
                String purchaseIdStr = paymentIntent.getMetadata().get("purchaseId");
                String userIdStr = paymentIntent.getMetadata().get("userId");

                if (purchaseIdStr != null) {
                    System.out.println("Confirming purchase: " + purchaseIdStr);
                    ticketService.confirmPurchase(UUID.fromString(purchaseIdStr));

                    if (userIdStr != null) {
                        System.out.println("Clearing cart for user: " + userIdStr);
                        cartService.clearCart(UUID.fromString(userIdStr));
                    } else {
                        System.out.println("UserId not found in metadata");
                    }

                    return ResponseEntity.ok("Payment confirmed and purchase fulfilled");
                } else {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Purchase ID not found in metadata");
                }
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Payment not succeeded: " + paymentIntent.getStatus());
            }
        } catch (com.stripe.exception.StripeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

}
