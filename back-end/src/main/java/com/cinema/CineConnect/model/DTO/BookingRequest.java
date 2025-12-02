package com.cinema.CineConnect.model.DTO;

import java.util.List;

public record BookingRequest(
        Long sessionId,
        String clientName,
        List<String> seats // Lista de assentos ex: ["A1", "A2"]
) {
}