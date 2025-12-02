package com.cinema.CineConnect.model.DTO;

import com.cinema.CineConnect.model.DTO.CinemaRecord;
import com.cinema.CineConnect.model.DTO.TheaterRecord;

import java.time.LocalDateTime;

// Este record será o objeto final enviado para o Front-end
public record SessionResponse(
        Long sessionId,
        LocalDateTime timeDate,
        String language,
        Long movieId, // Apenas o ID do filme é suficiente aqui
        CinemaRecord cinema,
        TheaterRecord theater) {
}