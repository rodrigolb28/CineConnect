package com.cinema.CineConnect.model.DTO;

import java.math.BigDecimal;

public record MovieRecord(
                Integer id,
                String title,
                String synopsis,
                String genre,
                Integer duration,
                BigDecimal rating,
                String imageFilename // Apenas o nome: "foto.jpg"
) {
        public String getFullImageUrl() {
                return "/uploads/" + imageFilename;
        }
}