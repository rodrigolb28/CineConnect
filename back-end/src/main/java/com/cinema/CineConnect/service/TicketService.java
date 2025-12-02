package com.cinema.CineConnect.service;

import com.cinema.CineConnect.model.DTO.BookingRequest;
import com.cinema.CineConnect.repository.PurchaseRepository;
import com.cinema.CineConnect.repository.TicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TicketService {

    private final PurchaseRepository purchaseRepository;
    private final com.cinema.CineConnect.repository.ProductRepository productRepository;
    private final TicketRepository ticketRepository;

    public TicketService(PurchaseRepository purchaseRepository,
            com.cinema.CineConnect.repository.ProductRepository productRepository,
            TicketRepository ticketRepository) {
        this.purchaseRepository = purchaseRepository;
        this.productRepository = productRepository;
        this.ticketRepository = ticketRepository;
    }

    // Atualiza tickets/pedidos no banco de dados usando o sessionId do Stripe
    public void markTicketsAsPaid(String stripeSessionId) {
        System.out.println("Processing payment for Stripe Session: " + stripeSessionId);
        com.cinema.CineConnect.model.Purchase purchase = purchaseRepository
                .retrievePurchaseByPaymentId(stripeSessionId);

        if (purchase != null) {
            System.out.println("Found purchase: " + purchase.getId());
            confirmPurchase(purchase.getId());
        } else {
            System.out.println("No purchase found for Stripe Session: " + stripeSessionId);
        }
    }

    public void confirmPurchase(java.util.UUID purchaseId) {
        System.out.println("Confirming purchase: " + purchaseId);
        purchaseRepository.updateStatus(purchaseId, "PAID");

        com.cinema.CineConnect.model.Purchase purchase = purchaseRepository.retrievePurchaseById(purchaseId);
        if (purchase != null) {
            for (com.cinema.CineConnect.model.PurchaseItem item : purchase.getItems()) {
                updateProductStock(item.getProductId(), item.getQuantity());

                // Check if item is a Ticket and save it
                com.cinema.CineConnect.model.DTO.ProductRecord productRecord = productRepository
                        .findById(item.getProductId());
                if (productRecord != null && "Ticket".equals(productRecord.type())) {
                    // Use user ID as client name if available, or a placeholder
                    String clientName = purchase.getUserId() != null ? purchase.getUserId().toString()
                            : "Unknown Client";
                    ticketRepository.saveTicket(productRecord.sessionId(), productRecord.seatNumber(), clientName);
                }

                if (item.getAddons() != null) {
                    for (com.cinema.CineConnect.model.PurchaseItem addon : item.getAddons()) {
                        updateProductStock(addon.getProductId(), addon.getQuantity());
                    }
                }
            }
        }
    }

    private void updateProductStock(java.util.UUID productId, int quantityPurchased) {
        Integer currentQuantity = productRepository.getQuantityById(productId);
        if (currentQuantity != null) {
            int newQuantity = currentQuantity - quantityPurchased;
            if (newQuantity < 0)
                newQuantity = 0; // Prevent negative stock
            productRepository.updateProductQuantity(productId, newQuantity);
        }
    }

    public List<String> getOccupiedSeats(Long sessionId) {
        return ticketRepository.findOccupiedSeats(sessionId);
    }

    @Transactional // Importante: Se um assento falhar, cancela tudo
    public void bookTickets(BookingRequest request) {
        List<String> occupied = ticketRepository.findOccupiedSeats(request.sessionId());

        for (String seat : request.seats()) {
            if (occupied.contains(seat)) {
                throw new IllegalArgumentException("O assento " + seat + " já está ocupado.");
            }
            // Salva o ticket
            ticketRepository.saveTicket(request.sessionId(), seat, request.clientName());
        }
    }
}
