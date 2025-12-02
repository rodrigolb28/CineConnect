package com.cinema.CineConnect.service;

import com.cinema.CineConnect.model.DTO.MovieRecord;
import com.cinema.CineConnect.repository.MovieRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class MovieService {

    private final JdbcClient jdbcClient;
    private final MovieRepository movieRepository; // Adicionado aqui
    private final Path rootLocation;

    private static final RowMapper<MovieRecord> MOVIE_ROW_MAPPER = (rs, rowNum) -> new MovieRecord(
            rs.getInt("id"),
            rs.getString("title"),
            rs.getString("synopsis"),
            rs.getString("genre"),
            rs.getInt("duration"),
            rs.getBigDecimal("rating"),
            rs.getString("image_filename"));

    // CORREÇÃO: Injetando o Repository via construtor
    public MovieService(JdbcClient jdbcClient,
            MovieRepository movieRepository,
            @Value("${file.upload-dir}") String uploadDir) {
        this.jdbcClient = jdbcClient;
        this.movieRepository = movieRepository;
        this.rootLocation = Paths.get(uploadDir);
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Não foi possível criar pasta de uploads", e);
        }
    }

    public MovieRecord saveMovieWithImage(MovieRecord movie, MultipartFile file) throws IOException {
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String uniqueFilename = UUID.randomUUID().toString() + "_" + originalFilename;

        Path destinationFile = this.rootLocation.resolve(Paths.get(uniqueFilename))
                .normalize().toAbsolutePath();

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
        }

        String sql = """
                INSERT INTO movies (title, synopsis, genre, duration, rating, image_filename)
                VALUES (?, ?, ?, ?, ?, ?)
                RETURNING id
                """;

        Integer generatedId = jdbcClient.sql(sql)
                .params(
                        movie.title(),
                        movie.synopsis(),
                        movie.genre(),
                        movie.duration(),
                        movie.rating(),
                        uniqueFilename)
                .query(Integer.class)
                .single();

        return new MovieRecord(
                generatedId, movie.title(), movie.synopsis(), movie.genre(),
                movie.duration(), movie.rating(), uniqueFilename);
    }

    public List<MovieRecord> findAll() {
        // Dica: Poderia usar o repository aqui também: return
        // movieRepository.findAll();
        return jdbcClient.sql("SELECT * FROM movies").query(MOVIE_ROW_MAPPER).list();
    }

    public Optional<MovieRecord> findById(Integer id) {
        // CORREÇÃO: Agora usa a instância injetada, não um "new"
        return movieRepository.findById(id);
    }
}