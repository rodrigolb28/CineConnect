package com.cinema.CineConnect.repository;

import com.cinema.CineConnect.model.DTO.CinemaRecord;
import com.cinema.CineConnect.model.DTO.SessionResponse;
import com.cinema.CineConnect.model.DTO.TheaterRecord;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class SessionRepository {

        private final JdbcClient jdbcClient;

        public SessionRepository(JdbcClient jdbcClient) {
                this.jdbcClient = jdbcClient;
        }

        // Método para listar todas as sessões de um filme
        public List<SessionResponse> findByMovieId(Long movieId) {
                String sql = """
                                    SELECT s.session_id, s.time_date, s.language, s.movie_id,
                                           c.id AS cinema_id, c.name AS cinema_name, c.location AS cinema_location,
                                           t.id AS theater_id
                                    FROM sessions s
                                    INNER JOIN cinemas c ON s.cinema_id = c.id
                                    INNER JOIN theaters t ON s.theater_id = t.id
                                    WHERE s.movie_id = :movieId
                                    ORDER BY c.name, s.time_date
                                """;

                return jdbcClient.sql(sql)
                                .param("movieId", movieId)
                                .query((rs, rowNum) -> new SessionResponse(
                                                rs.getLong("session_id"),
                                                rs.getTimestamp("time_date").toLocalDateTime(),
                                                rs.getString("language"),
                                                rs.getLong("movie_id"),
                                                new CinemaRecord(
                                                                rs.getLong("cinema_id"),
                                                                rs.getString("cinema_name"),
                                                                rs.getString("cinema_location")),
                                                new TheaterRecord(
                                                                rs.getLong("theater_id"))))
                                .list();
        }

        // Método para buscar uma sessão específica pelo ID (usado no checkout)
        public Optional<SessionResponse> findById(Long id) {
                String sql = """
                                    SELECT s.session_id, s.time_date, s.language, s.movie_id,
                                           c.id AS cinema_id, c.name AS cinema_name, c.location AS cinema_location,
                                           t.id AS theater_id
                                    FROM sessions s
                                    INNER JOIN cinemas c ON s.cinema_id = c.id
                                    INNER JOIN theaters t ON s.theater_id = t.id
                                    WHERE s.session_id = :id
                                """;

                return jdbcClient.sql(sql)
                                .param("id", id)
                                .query((rs, rowNum) -> new SessionResponse(
                                                rs.getLong("session_id"),
                                                rs.getTimestamp("time_date").toLocalDateTime(),
                                                rs.getString("language"),
                                                rs.getLong("movie_id"),
                                                new CinemaRecord(
                                                                rs.getLong("cinema_id"),
                                                                rs.getString("cinema_name"),
                                                                rs.getString("cinema_location")),
                                                new TheaterRecord(
                                                                rs.getLong("theater_id"))))
                                .optional();
        }
}