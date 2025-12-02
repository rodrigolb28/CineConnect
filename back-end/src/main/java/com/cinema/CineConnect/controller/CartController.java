package com.cinema.CineConnect.controller;

import com.cinema.CineConnect.model.Cart;

import com.cinema.CineConnect.repository.UserRepository;
import com.cinema.CineConnect.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;
    private final UserRepository userRepository;

    public CartController(CartService cartService, UserRepository userRepository) {
        this.cartService = cartService;
        this.userRepository = userRepository;
    }

    private UUID getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof Jwt) {
            String userIdStr = authentication.getName();
            try {
                return UUID.fromString(userIdStr);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        return null;
    }

    @GetMapping
    public ResponseEntity<Cart> getCart() {
        UUID userId = getAuthenticatedUserId();
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(cartService.getCart(userId));
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/items")
    @SuppressWarnings("unchecked")
    public ResponseEntity<Void> addItem(@AuthenticationPrincipal Jwt jwt, @RequestBody Map<String, Object> payload) {
        UUID uuid = UUID.fromString(jwt.getSubject());
        if (uuid == null) {
            return ResponseEntity.status(401).build();
        }

        String productIdStr = (String) payload.get("productId");
        Integer quantity = (Integer) payload.get("quantity");
        List<String> addonIdsStr = (List<String>) payload.get("addonIds");

        if (productIdStr == null && !payload.containsKey("type")) {
            return ResponseEntity.badRequest().build();
        }

        if ("TICKET".equals(payload.get("type"))) {
            Long sessionId = ((Number) payload.get("sessionId")).longValue();
            String seatNumber = (String) payload.get("seatNumber");
            Double priceDouble = ((Number) payload.get("price")).doubleValue();
            java.math.BigDecimal price = java.math.BigDecimal.valueOf(priceDouble);

            cartService.addTicketToCart(uuid, sessionId, seatNumber, price);
            return ResponseEntity.ok().build();
        }

        if (productIdStr == null || quantity == null) {
            return ResponseEntity.badRequest().build();
        }

        UUID productId = UUID.fromString(productIdStr);
        List<UUID> addonIds = addonIdsStr != null ? addonIdsStr.stream().map(UUID::fromString).toList() : null;

        cartService.addItemToCart(uuid, productId, quantity, addonIds);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<Void> updateItemQuantity(@PathVariable UUID itemId,
            @RequestBody Map<String, Integer> payload) {
        UUID userId = getAuthenticatedUserId();
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        Integer quantity = payload.get("quantity");
        if (quantity == null) {
            return ResponseEntity.badRequest().build();
        }

        cartService.updateItemQuantity(userId, itemId, quantity);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> removeItem(@PathVariable UUID itemId) {
        UUID userId = getAuthenticatedUserId();
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        cartService.removeItem(userId, itemId);
        return ResponseEntity.ok().build();
    }
}
