package com.cinema.CineConnect.model;

import java.math.BigDecimal;
import java.util.UUID;

public class Ticket extends Product {
    Long sessionId;
    String seatNumber;

    public Ticket(UUID productId, Long sessionId, String seatNumber, String name, String type, BigDecimal price) {
        super(productId, name, type, price, 100, true); // Default values for Ticket
        this.sessionId = sessionId;
        this.seatNumber = seatNumber;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public String getSeatNumber() {

        return seatNumber;
    }

    @Override
    public java.util.List<Product> getInfo() {
        return new java.util.ArrayList<>();
    }
}
