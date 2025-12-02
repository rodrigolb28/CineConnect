package com.cinema.CineConnect.model.DTO;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ProductRecord(
        UUID productId,
        String name,
        String type,
        BigDecimal price,
        int quantity,
        boolean available,
        Long sessionId,
        String imageUrl,
        List<ProductRecord> addOns,
        String seatNumber) {
}
