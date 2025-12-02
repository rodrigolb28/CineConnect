package com.cinema.CineConnect.repository;

import com.cinema.CineConnect.model.DTO.MovieRecord;
import org.springframework.jdbc.core.RowMapper; // Importe isso
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class MovieRepository {

    private final JdbcClient jdbcClient;

    public MovieRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    // Copiamos o mesmo mapper que funcionou no Service para garantir consistência
    private static final RowMapper<MovieRecord> MOVIE_ROW_MAPPER = (rs, rowNum) -> new MovieRecord(
            rs.getInt("id"),
            rs.getString("title"),
            rs.getString("synopsis"),
            rs.getString("genre"),
            rs.getInt("duration"),
            rs.getBigDecimal("rating"),
            rs.getString("image_filename") // O segredo está aqui! Lendo a coluna com underline
    );

    public List<MovieRecord> findAll() {
        return jdbcClient.sql("SELECT * FROM movies")
                .query(MOVIE_ROW_MAPPER) // Usando o mapper manual
                .list();
    }

    public Optional<MovieRecord> findByName(String name) {
        return jdbcClient.sql("SELECT * FROM movies WHERE name = :name")
                .param("name", name)
                .query(MOVIE_ROW_MAPPER)
                .optional();
    }

    public Optional<MovieRecord> findById(Integer id) {
        var sql = "SELECT * FROM movies WHERE id = :id";

        return jdbcClient.sql(sql)
                .param("id", id)
                .query(MOVIE_ROW_MAPPER)
                .optional();
    }
}