package com.cinema.CineConnect.service;

import com.cinema.CineConnect.model.Cart;
import com.cinema.CineConnect.model.DTO.ProductRecord;
import com.cinema.CineConnect.repository.CartRepository;
import com.cinema.CineConnect.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    private final com.cinema.CineConnect.repository.TicketRepository ticketRepository;

    public CartService(CartRepository cartRepository, ProductRepository productRepository,
            com.cinema.CineConnect.repository.TicketRepository ticketRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.ticketRepository = ticketRepository;
    }

    @Transactional
    public Cart getCart(UUID userId) {
        return getCartDetails(getOrCreateCartId(userId));
    }

    @Transactional
    public void addItemToCart(UUID userId, UUID productId, Integer quantity, List<UUID> addonIds) {
        UUID cartId = getOrCreateCartId(userId);

        ProductRecord product = productRepository.findById(productId);
        if (product == null) {
            throw new IllegalArgumentException("Product not found: " + productId);
        }

        UUID cartItemId = cartRepository.addItem(cartId, productId, quantity, product.price());

        if (addonIds != null && !addonIds.isEmpty()) {
            addAddonsToItem(cartItemId, addonIds);
        }

        cartRepository.updateCartPrice(cartId);
    }

    @Transactional
    public void updateItemQuantity(UUID userId, UUID cartItemId, int quantity) {
        UUID cartId = getOrCreateCartId(userId);

        if (quantity <= 0) {
            cartRepository.removeItem(cartItemId);
        } else {
            // Check if item is a ticket
            com.cinema.CineConnect.model.CartItem item = cartRepository.getItem(cartItemId);
            if (item != null && item.getProduct() instanceof com.cinema.CineConnect.model.Ticket) {
                // Tickets cannot have quantity > 1
                if (quantity > 1) {
                    throw new IllegalArgumentException(
                            "Não é possível adicionar mais de um ingresso para o mesmo assento.");
                }
            }
            cartRepository.updateItemQuantity(cartItemId, quantity);
        }
        cartRepository.updateCartPrice(cartId);
    }

    @Transactional
    public void removeItem(UUID userId, UUID cartItemId) {
        UUID cartId = getOrCreateCartId(userId);
        cartRepository.removeItem(cartItemId);
        cartRepository.updateCartPrice(cartId);
    }

    @Transactional
    public void clearCart(UUID userId) {
        System.out.println("CartService: Clearing cart for user " + userId);
        UUID cartId = getOrCreateCartId(userId);
        System.out.println("CartService: Cart ID is " + cartId);
        cartRepository.clearCart(cartId);
        cartRepository.updateCartPrice(cartId);
        System.out.println("CartService: Cart cleared and price updated");
    }

    private UUID getOrCreateCartId(UUID userId) {
        UUID cartId = cartRepository.findByUserId(userId);
        if (cartId == null) {
            Cart newCart = new Cart();
            newCart.setUserId(userId);
            newCart.setPrice(BigDecimal.ZERO);
            newCart.setName("User Cart");
            cartId = cartRepository.createCart(newCart);
        }
        return cartId;
    }

    private Cart getCartDetails(UUID cartId) {
        return cartRepository.getCartDetails(cartId);
    }

    private void addAddonsToItem(UUID cartItemId, List<UUID> addonIds) {
        for (UUID addonId : addonIds) {
            ProductRecord addon = productRepository.findById(addonId);
            if (addon != null) {
                cartRepository.addAddon(cartItemId, addonId, 1, addon.price());
            }
        }
    }

    @Transactional
    public void addTicketToCart(UUID userId, Long sessionId, String seatNumber, BigDecimal price) {
        // Validation 1: Check if seat is already occupied (bought)
        List<String> occupiedSeats = ticketRepository.findOccupiedSeats(sessionId);
        if (occupiedSeats.contains(seatNumber)) {
            throw new IllegalArgumentException("O assento " + seatNumber + " já está ocupado.");
        }

        UUID cartId = getOrCreateCartId(userId);

        // Validation 2: Check if seat is already in the cart
        if (cartRepository.isSeatInCart(cartId, sessionId, seatNumber)) {
            throw new IllegalArgumentException("O assento " + seatNumber + " já está no seu carrinho.");
        }

        // Create a new Ticket product
        UUID productId = UUID.randomUUID();
        // Assuming "Ticket" type exists in DB. If not, migration
        // V1__createRolesAndPopulate.sql or similar should have it.
        // Based on ProductFactory, type is "TICKET".
        // Name can be "Ticket - Session X - Seat Y"
        String name = "Ingresso - Sessão " + sessionId + " - Assento " + seatNumber;

        com.cinema.CineConnect.model.Ticket ticket = new com.cinema.CineConnect.model.Ticket(
                productId, sessionId, seatNumber, name, "Ticket", price);

        // Persist the ticket product
        productRepository.saveProduct(ticket);

        // Add to cart
        cartRepository.addItem(cartId, productId, 1, price);
        cartRepository.updateCartPrice(cartId);
    }
}
